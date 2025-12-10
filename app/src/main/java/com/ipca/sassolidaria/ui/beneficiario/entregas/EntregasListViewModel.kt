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
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
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
    val dataHora: String
)

class EntregasListViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var entregasState: ResultState<List<EntregaUi>> by mutableStateOf(ResultState.Loading)
        private set

    // estado para o popup de detalhes (lista de itens da entrega)
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
                // 1) Buscar o beneficiário deste utilizador
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

                // 2) Listener em tempo real dos agendamentos desse beneficiário
                startAgendamentosListener(beneficiarioId)

            } catch (e: Exception) {
                entregasState = ResultState.Error(
                    e.message ?: "Erro ao carregar dados do beneficiário."
                )
            }
        }
    }

    private fun startAgendamentosListener(beneficiarioId: String) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection(FirestoreCollections.AGENDAMENTOS)
            .whereEqualTo("id_beneficiario", beneficiarioId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    entregasState = ResultState.Error("Erro ao ouvir agendamentos: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    entregasState = ResultState.Error("Erro ao carregar agendamentos.")
                    return@addSnapshotListener
                }

                val uiList = snapshot.documents.mapNotNull { doc ->
                    val agendamento = doc.toObject(Agendamento::class.java) ?: return@mapNotNull null
                    val idAgendamento = agendamento.id_agendamento.ifBlank { doc.id }

                    // 🔁 Conversão EstadoAgendamento -> EstadoEntrega
                    val estadoEntrega = when (agendamento.estado) {
                        EstadoAgendamento.PENDENTE -> EstadoEntrega.PENDENTE
                        EstadoAgendamento.ACEITE   -> EstadoEntrega.PRONTA
                        EstadoAgendamento.RECUSADO -> EstadoEntrega.CANCELADA
                        else -> EstadoEntrega.PENDENTE
                    }

                    EntregaUi(
                        id = idAgendamento,
                        estado = estadoEntrega,
                        dataHora = agendamento.data_agendada?.let { dateTimeFormat.format(it) } ?: ""
                    )
                }

                entregasState = ResultState.Success(uiList)
            }
    }

    // ---------- DETALHES: ITENS DA ENTREGA LIGADA A ESTE AGENDAMENTO ----------

    fun carregarItensEntrega(agendamentoId: String) {
        detalhesState = ResultState.Loading

        viewModelScope.launch {
            try {
                // 1) Buscar a entrega ligada a este agendamento
                val entregaSnap = db.collection(FirestoreCollections.ENTREGAS)
                    .whereEqualTo("id_agendamento", agendamentoId)
                    .limit(1)
                    .get()
                    .await()

                val entregaDoc = entregaSnap.documents.firstOrNull()
                if (entregaDoc == null) {
                    detalhesState = ResultState.Error("Ainda não existem itens para esta entrega.")
                    return@launch
                }

                val entrega = entregaDoc.toObject(Entrega::class.java)

                // possíveis ids que podem estar gravados em itens_entrega
                val possibleIds = mutableListOf<String>()

                entrega?.id_entrega
                    ?.takeIf { it.isNotBlank() }
                    ?.let { possibleIds.add(it) }

                possibleIds.add(entregaDoc.id)

                // 2) Buscar itens_entrega usando whereIn
                val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                    .whereIn("id_entrega", possibleIds.distinct())
                    .get()
                    .await()

                if (itensSnap.isEmpty) {
                    detalhesState = ResultState.Error("Nenhum item associado a esta entrega.")
                    return@launch
                }

                val linhas = mutableListOf<String>()

                for (itemDoc in itensSnap.documents) {
                    val item = itemDoc.toObject(ItemEntrega::class.java) ?: continue

                    // 3) Buscar produto para obter o nome
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
