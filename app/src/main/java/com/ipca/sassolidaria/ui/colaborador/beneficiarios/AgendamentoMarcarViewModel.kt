package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.PreferenciaAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import com.ipca.sassolidaria.data.repository.AgendamentosRepository
import com.ipca.sassolidaria.data.repository.ApoiosRepository
import com.ipca.sassolidaria.data.repository.PedidosRepository
import com.ipca.sassolidaria.data.repository.PreferenciasRepository
import kotlinx.coroutines.launch
import java.util.Date

data class MarcarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val preferencia: PreferenciaAgendamento? = null
)

class AgendamentoMarcarViewModel(
    private val agRepo: AgendamentosRepository = AgendamentosRepository(),
    private val pedRepo: PedidosRepository = PedidosRepository(),
    private val apRepo: ApoiosRepository = ApoiosRepository(),
    private val prefRepo: PreferenciasRepository = PreferenciasRepository()
) : ViewModel() {

    var uiState by mutableStateOf(MarcarUiState())
        private set

    fun carregarPreferencias(idBeneficiario: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val pref = prefRepo.getUltimaPreferencia(idBeneficiario)
                uiState = uiState.copy(isLoading = false, preferencia = pref)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar preferências")
            }
        }
    }

    fun confirmar(
        idBeneficiario: String,
        tipo: Tipo,
        ref: String,
        data: Date,
        onOk: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val idAg = agRepo.criarAgendamento(idBeneficiario, data, tipo)

                when (tipo) {
                    Tipo.PEDIDO -> {
                        pedRepo.aceitarPedidoEAssociarAgendamento(ref, idAg)
                    }
                    Tipo.APOIO -> {
                        val mes = ref.toIntOrNull() ?: 0
                        apRepo.criarApoio(idBeneficiario, mes, idAg)
                    }
                }

                uiState = uiState.copy(isLoading = false)
                onOk()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao marcar agendamento")
            }
        }
    }
}
