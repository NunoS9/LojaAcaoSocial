package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.PreferenciaAgendamento
import com.ipca.sassolidaria.data.repository.AgendamentosRepository
import com.ipca.sassolidaria.data.repository.PreferenciasRepository
import kotlinx.coroutines.launch
import java.util.Date

data class AgendamentoEditarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val preferencia: PreferenciaAgendamento? = null,
    val agendamento: Agendamento? = null
)

class AgendamentoEditarViewModel(
    private val agRepo: AgendamentosRepository = AgendamentosRepository(),
    private val prefRepo: PreferenciasRepository = PreferenciasRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AgendamentoEditarUiState())
        private set

    fun carregar(idBeneficiario: String, idAgendamento: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val pref = prefRepo.getUltimaPreferencia(idBeneficiario)
                val ag = agRepo.getAgendamentoById(idAgendamento)
                uiState = uiState.copy(isLoading = false, preferencia = pref, agendamento = ag)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar dados")
            }
        }
    }

    fun confirmarEdicao(idAgendamento: String, novaData: Date, onOk: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                agRepo.editarDataEReabrirAgendamento(idAgendamento, novaData)
                uiState = uiState.copy(isLoading = false)
                onOk()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao editar agendamento")
            }
        }
    }
}
