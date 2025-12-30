package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.PreferenciaAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.AgendamentosRepository
import com.ipca.sassolidaria.data.repository.ApoiosRepository
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.data.repository.PedidosRepository
import com.ipca.sassolidaria.data.repository.PreferenciasRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Month
import java.time.format.TextStyle

data class MarcarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val preferencia: PreferenciaAgendamento? = null
)

class AgendamentoMarcarViewModel(
    private val agRepo: AgendamentosRepository = AgendamentosRepository(),
    private val pedRepo: PedidosRepository = PedidosRepository(),
    private val apRepo: ApoiosRepository = ApoiosRepository(),
    private val prefRepo: PreferenciasRepository = PreferenciasRepository(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                // 1) criar agendamento
                val idAg = agRepo.criarAgendamento(idBeneficiario, data, tipo)

                // 2) associar ao pedido/apoio
                when (tipo) {
                    Tipo.PEDIDO -> {
                        pedRepo.aceitarPedidoEAssociarAgendamento(ref, idAg)
                    }
                    Tipo.APOIO -> {
                        val mes = ref.toIntOrNull() ?: 0
                        apRepo.criarApoio(idBeneficiario, mes, idAg)
                    }
                }

                // 3) notificar beneficiário com mensagem DIFERENTE
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                    val dataLabel = sdf.format(data)

                    val (titulo, conteudo) = when (tipo) {
                        Tipo.PEDIDO -> {
                            // ref = idPedido
                            "Pedido aceite" to
                                    "O seu pedido (#$ref) foi aceite. Agendamento marcado para $dataLabel (Agendamento #$idAg)."
                        }

                        Tipo.APOIO -> {
                            val mesNumero = ref.toIntOrNull() ?: 1

                            val nomeMes = Month.of(mesNumero)
                                .getDisplayName(TextStyle.FULL, Locale("pt", "PT"))

                            "Apoio agendado" to
                                    "O seu apoio referente a $nomeMes foi agendado para $dataLabel (Agendamento #$idAg)."
                        }
                    }

                    notifRepo.notificarDestinatario(
                        destinatarioId = idBeneficiario,
                        tipo = TipoNotificacao.NOVO_AGENDAMENTO,
                        titulo = titulo,
                        conteudo = conteudo
                    )
                } catch (_: Exception) {
                }

                uiState = uiState.copy(isLoading = false)
                onOk()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao marcar agendamento")
            }
        }
    }
}
