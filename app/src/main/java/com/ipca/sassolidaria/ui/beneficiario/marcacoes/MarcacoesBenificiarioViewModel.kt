package com.ipca.sassolidaria.ui.beneficiario.marcacoes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.data.repository.AgendamentosRepository
import com.ipca.sassolidaria.data.repository.EntregasRepository
import kotlinx.coroutines.launch
import java.util.Date

data class MarcacoesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filtro: EstadoAgendamento? = null,   // null = Todos
    val agendamentos: List<Agendamento> = emptyList()
)

class MarcacoesBeneficiarioViewModel(
    private val agRepo: AgendamentosRepository = AgendamentosRepository(),
    private val entRepo: EntregasRepository = EntregasRepository()
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
        // só permite alterar se estiver pendente
        if (agendamento.estado != EstadoAgendamento.PENDENTE) return

        viewModelScope.launch {
            try {
                // 1) Atualizar estado do agendamento
                agRepo.atualizarEstadoAgendamento(
                    agendamento.id_agendamento,
                    EstadoAgendamento.ACEITE
                )

                // 2) Criar entrega associada (usa o id_beneficiario do próprio agendamento)
                val entrega = Entrega(
                    id_entrega = "",
                    tipo = agendamento.tipo,
                    id_beneficiario = agendamento.id_beneficiario,
                    id_agendamento = agendamento.id_agendamento,
                    id_colaborador = "",          // podes preencher noutro fluxo
                    estado = EstadoEntrega.PENDENTE
                )

                entRepo.criarEntrega(entrega)

                carregarAgendamentos()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Erro ao confirmar agendamento")
            }
        }
    }

    fun recusarAgendamento(agendamento: Agendamento) {
        if (agendamento.estado != EstadoAgendamento.PENDENTE) return

        viewModelScope.launch {
            try {
                agRepo.atualizarEstadoAgendamento(
                    agendamento.id_agendamento,
                    EstadoAgendamento.RECUSADO
                )
                carregarAgendamentos()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Erro ao recusar agendamento")
            }
        }
    }
}
