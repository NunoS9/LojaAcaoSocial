package com.ipca.sassolidaria.ui.beneficiario.marcacoes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.AgendamentosRepository
import com.ipca.sassolidaria.data.repository.EntregasRepository
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class MarcacoesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filtro: EstadoAgendamento? = null,   // null = Todos
    val agendamentos: List<Agendamento> = emptyList()
)

class MarcacoesBeneficiarioViewModel(
    private val agRepo: AgendamentosRepository = AgendamentosRepository(),
    private val entRepo: EntregasRepository = EntregasRepository(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
) : ViewModel() {

    var uiState by mutableStateOf(MarcacoesUiState())
        private set

    init {
        carregarAgendamentos()
    }

    fun carregarAgendamentos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val lista = agRepo.getAgendamentosDoBeneficiario()
                    .sortedByDescending { it.data_agendada ?: Date(0) }

                uiState = uiState.copy(
                    isLoading = false,
                    agendamentos = lista
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar marcações"
                )
            }
        }
    }

    fun alterarFiltro(novo: EstadoAgendamento?) {
        uiState = uiState.copy(filtro = novo)
    }

    fun confirmarAgendamento(agendamento: Agendamento) {
        if (agendamento.estado != EstadoAgendamento.PENDENTE) return

        viewModelScope.launch {
            try {
                // buscar nome do beneficiário
                val nomeBeneficiario = obterNomeBeneficiarioAtual()

                // 1) Atualizar estado do agendamento
                agRepo.atualizarEstadoAgendamento(
                    agendamento.id_agendamento,
                    EstadoAgendamento.ACEITE
                )

                // 2) Criar entrega associada
                val entrega = Entrega(
                    id_entrega = "",
                    tipo = agendamento.tipo,
                    id_beneficiario = agendamento.id_beneficiario,
                    id_agendamento = agendamento.id_agendamento,
                    id_colaborador = "", // mantém igual ao teu fluxo
                    estado = EstadoEntrega.PENDENTE
                )
                entRepo.criarEntrega(entrega)

                // 3) notificar TODOS os colaboradores
                try {
                    notifRepo.notificarTodosColaboradores(
                        tipo = TipoNotificacao.AGENDAMENTO_ATUALIZADO,
                        titulo = "Agendamento aceite",
                        conteudo = "$nomeBeneficiario aceitou o agendamento #${agendamento.id_agendamento}."
                    )
                } catch (_: Exception) {
                }

                carregarAgendamentos()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message ?: "Erro ao confirmar agendamento")
            }
        }
    }

    fun recusarAgendamento(agendamento: Agendamento) {
        if (agendamento.estado != EstadoAgendamento.PENDENTE) return

        viewModelScope.launch {
            try {
                //  buscar nome do beneficiário
                val nomeBeneficiario = obterNomeBeneficiarioAtual()

                // 1) Atualizar estado
                agRepo.atualizarEstadoAgendamento(
                    agendamento.id_agendamento,
                    EstadoAgendamento.RECUSADO
                )

                // 2) notificar TODOS os colaboradores
                try {
                    notifRepo.notificarTodosColaboradores(
                        tipo = TipoNotificacao.AGENDAMENTO_ATUALIZADO,
                        titulo = "Agendamento recusado",
                        conteudo = "$nomeBeneficiario recusou o agendamento #${agendamento.id_agendamento}."
                    )
                } catch (_: Exception) {
                }

                carregarAgendamentos()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message ?: "Erro ao recusar agendamento")
            }
        }
    }

    private suspend fun obterNomeBeneficiarioAtual(): String {
        val uid = auth.currentUser?.uid ?: return "Beneficiário"

        val benefSnapshot = db.collection(FirestoreCollections.BENEFICIARIOS)
            .whereEqualTo("id_utilizador", uid)
            .limit(1)
            .get()
            .await()

        val benefDoc = benefSnapshot.documents.firstOrNull()
        return benefDoc?.getString("nome") ?: "Beneficiário"
    }
}
