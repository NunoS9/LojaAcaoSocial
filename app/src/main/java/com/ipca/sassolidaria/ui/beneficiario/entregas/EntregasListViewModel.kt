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
import com.ipca.sassolidaria.data.model.Beneficiario
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
    val id: String,                 // id_entrega (ou id do doc)
    val estado: EstadoEntrega,
    val dataHora: String            // data do agendamento formatada
)

class EntregasListViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var entregasState: ResultState<List<EntregaUi>> by mutableStateOf(ResultState.Loading)
        private set

    // estado para o popup de detalhes (lista de itens dessa entrega)
    var detalhesState: ResultState<List<String>> by mutableStateOf(ResultState.Idle)
        private set

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())

    private var listenerRegistration: ListenerRegistration? = null

    init {
        carregarEntregasDoBeneficiario()
    }

    private fun carregarEntregasDoBeneficiario() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            entregasState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        entregasState = ResultState.Loading

        viewModelScope.launch {
            try {
                // 1) Buscar o beneficiário ligado a este utilizador
                val benefSnap = db.collection(FirestoreCollections.BENEFICIARIOS)
                    .whereEqualTo("id_utilizador", uid)
                    .limit(1)
                    .get()
                    .await()

                val benefDoc = benefSnap.documents.firstOrNull()
                if (benefDoc == null) {
                    entregasState = ResultState.Error("Beneficiário não encontrado para este utilizador.")
                    return@launch
                }

                val benef = benefDoc.toObject(Beneficiario::class.java)
                val beneficiarioId =
                    benef?.id_beneficiario.takeUnless { it.isNullOrBlank() } ?: benefDoc.id

                // 2) Listener em tempo real das ENTREGAS desse beneficiário
                startEntregasListener(beneficiarioId)

            } catch (e: Exception) {
                entregasState = ResultState.Error(
                    e.message ?: "Erro ao carregar dados do beneficiário."
                )
            }
        }
    }

    private fun startEntregasListener(beneficiarioId: String) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection(FirestoreCollections.ENTREGAS)
            .whereEqualTo("id_beneficiario", beneficiarioId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    entregasState = ResultState.Error("Erro ao ouvir entregas: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    entregasState = ResultState.Error("Erro ao carregar entregas.")
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val uiList = snapshot.documents.mapNotNull { doc ->
                            val entrega = doc.toObject(Entrega::class.java) ?: return@mapNotNull null
                            val idEntrega = entrega.id_entrega.ifBlank { doc.id }

                            // buscar data do agendamento (se existir)
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

                            EntregaUi(
                                id = idEntrega,
                                estado = entrega.estado,
                                dataHora = dataHora
                            )
                        }

                        entregasState = ResultState.Success(uiList)
                    } catch (ex: Exception) {
                        entregasState = ResultState.Error(
                            ex.message ?: "Erro ao processar entregas."
                        )
                    }
                }
            }
    }

    // ---------- DETALHES: ITENS DA ENTREGA ----------

    fun carregarItensEntrega(entregaId: String) {
        detalhesState = ResultState.Loading

        viewModelScope.launch {
            try {
                // 1) tentar apanhar o doc de entrega pelo id passado
                val entregaDocDirect = db.collection(FirestoreCollections.ENTREGAS)
                    .document(entregaId)
                    .get()
                    .await()

                val entregaDoc = if (entregaDocDirect.exists()) {
                    entregaDocDirect
                } else {
                    // se entregaId não for o docId, pode ser o campo id_entrega
                    val snap = db.collection(FirestoreCollections.ENTREGAS)
                        .whereEqualTo("id_entrega", entregaId)
                        .limit(1)
                        .get()
                        .await()
                    snap.documents.firstOrNull()
                }

                if (entregaDoc == null) {
                    detalhesState = ResultState.Error("Entrega não encontrada.")
                    return@launch
                }

                val entrega = entregaDoc.toObject(Entrega::class.java)

                val possibleEntregaIds = mutableListOf<String>()
                possibleEntregaIds.add(entregaDoc.id)
                entrega?.id_entrega
                    ?.takeIf { it.isNotBlank() }
                    ?.let { possibleEntregaIds.add(it) }

                // 2) buscar itens_entrega que apontam para esta entrega
                val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                    .whereIn("id_entrega", possibleEntregaIds.distinct())
                    .get()
                    .await()

                if (itensSnap.isEmpty) {
                    detalhesState = ResultState.Error("Nenhum item associado a esta entrega.")
                    return@launch
                }

                val linhas = mutableListOf<String>()

                for (itemDoc in itensSnap.documents) {
                    val item = itemDoc.toObject(ItemEntrega::class.java) ?: continue

                    val prodSnap = db.collection(FirestoreCollections.PRODUTOS)
                        .document(item.id_produto)
                        .get()
                        .await()

                    val produto = prodSnap.toObject(Produto::class.java)
                    val nome = produto?.nome ?: "Produto"

                    linhas.add("$nome (${item.quantidade})")
                }

                detalhesState = ResultState.Success(linhas)

            } catch (e: Exception) {
                detalhesState = ResultState.Error(
                    e.message ?: "Erro ao carregar itens da entrega."
                )
            }
        }
    }

    fun limparDetalhes() {
        detalhesState = ResultState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
