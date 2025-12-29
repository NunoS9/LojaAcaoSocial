package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.PreferenciaAgendamento
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.AgendamentosRepository
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.data.repository.PreferenciasRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AgendamentoEditarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val preferencia: PreferenciaAgendamento? = null,
    val agendamento: Agendamento? = null
)

class AgendamentoEditarViewModel(
    private val agRepo: AgendamentosRepository = AgendamentosRepository(),
    private val prefRepo: PreferenciasRepository = PreferenciasRepository(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AgendamentoEditarUiState())
        private set

    // ✅ guardamos aqui para não precisares de mudar a View
    private var currentBeneficiarioId: String? = null

    fun carregar(idBeneficiario: String, idAgendamento: String) {
        currentBeneficiarioId = idBeneficiario

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

    // ✅ assinatura igual à tua (não mexes na View)
    fun confirmarEdicao(idAgendamento: String, novaData: Date, onOk: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                // 1) editar e reabrir
                agRepo.editarDataEReabrirAgendamento(idAgendamento, novaData)

                // 2) notificar beneficiário
                val idBenef = currentBeneficiarioId
                if (!idBenef.isNullOrBlank()) {
                    try {
                        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                        val dataLabel = sdf.format(novaData)

                        // ⚠️ precisa existir no repo (ver nota abaixo)
                        notifRepo.notificarDestinatario(
                            destinatarioId = idBenef,
                            tipo = TipoNotificacao.NOVO_AGENDAMENTO,
                            titulo = "Agendamento alterado",
                            conteudo = "O seu agendamento (#$idAgendamento) foi alterado. Nova data: $dataLabel."
                        )
                    } catch (_: Exception) {
                        // ignora para não falhar o fluxo principal
                    }
                }

                uiState = uiState.copy(isLoading = false)
                onOk()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao editar agendamento")
            }
        }
    }
}
