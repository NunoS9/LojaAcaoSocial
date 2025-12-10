package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.ItemEntrega
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class EntregaUi(
    val id: String,
    val estado: EstadoEntrega,
    val dataHora: String,
    val itens: List<String>
)

class EntregasListViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var entregasState: ResultState<List<EntregaUi>> by mutableStateOf(ResultState.Loading)
        private set

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())

    private var listenerRegistration: ListenerRegistration? = null

    init {
        observarEntregasBeneficiario()
    }

    private fun observarEntregasBeneficiario() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            entregasState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        entregasState = ResultState.Loading

        // Se já houver listener, removemos
        listenerRegistration?.remove()

        listenerRegistration = db.collection(FirestoreCollections.ENTREGAS)
            .whereEqualTo("id_beneficiario", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    entregasState = ResultState.Error("Erro ao ouvir entregas: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    entregasState = ResultState.Error("Erro ao carregar entregas.")
                    return@addSnapshotListener
                }

                // Como precisamos de awaits (agendamento + itens + produtos),
                // fazemos o processamento dentro de uma coroutine
                viewModelScope.launch {
                    try {
                        val uiList = snapshot.documents.mapNotNull { doc ->
                            val entrega = doc.toObject(Entrega::class.java) ?: return@mapNotNull null
                            val idEntrega = entrega.id_entrega.ifBlank { doc.id }

                            // Buscar data/hora do agendamento
                            val dataHora = if (entrega.id_agendamento.isNotBlank()) {
                                val agSnap = db.collection(FirestoreCollections.AGENDAMENTOS)
                                    .document(entrega.id_agendamento)
                                    .get()
                                    .await()
                                val agendamento = agSnap.toObject(Agendamento::class.java)
                                agendamento?.data_agendada?.let { dateTimeFormat.format(it) } ?: ""
                            } else {
                                ""
                            }

                            // Só carregamos itens quando a entrega já estiver pronta ou entregue
                            val itensDescricao: List<String> =
                                if (entrega.estado == EstadoEntrega.PRONTA ||
                                    entrega.estado == EstadoEntrega.ENTREGUE
                                ) {
                                    val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                                        .whereEqualTo("id_entrega", idEntrega)
                                        .get()
                                        .await()

                                    itensSnap.documents.mapNotNull { itemDoc ->
                                        val item = itemDoc.toObject(ItemEntrega::class.java)
                                            ?: return@mapNotNull null

                                        // Buscar nome do produto
                                        val prodSnap = db.collection(FirestoreCollections.PRODUTOS)
                                            .document(item.id_produto)
                                            .get()
                                            .await()

                                        val produto = prodSnap.toObject(Produto::class.java)
                                        val nome = produto?.nome ?: "Produto"
                                        "$nome (${item.quantidade})"
                                    }
                                } else {
                                    emptyList()
                                }

                            EntregaUi(
                                id = idEntrega,
                                estado = entrega.estado,
                                dataHora = dataHora,
                                itens = itensDescricao
                            )
                        }

                        entregasState = ResultState.Success(uiList)
                    } catch (ex: Exception) {
                        entregasState = ResultState.Error(ex.message ?: "Erro ao processar entregas.")
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
