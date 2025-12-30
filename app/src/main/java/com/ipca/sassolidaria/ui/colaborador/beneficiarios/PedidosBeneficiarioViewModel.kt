package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.data.repository.PedidosRepository
import kotlinx.coroutines.launch
import java.util.Date

data class PedidosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pedidos: List<com.ipca.sassolidaria.data.model.Pedido> = emptyList()
)

class PedidosBeneficiarioViewModel(
    private val repo: PedidosRepository = PedidosRepository(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
) : ViewModel() {

    var uiState by mutableStateOf(PedidosUiState())
        private set

    fun carregar(idBeneficiario: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val lista = repo.getPedidosDoBeneficiario(idBeneficiario)
                    .sortedByDescending { it.data_pedido ?: Date(0) }
                uiState = uiState.copy(isLoading = false, pedidos = lista)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar pedidos")
            }
        }
    }

    fun cancelar(idPedido: String, idBeneficiario: String) {
        viewModelScope.launch {
            try {
                // 1) cancelar no Firestore
                repo.cancelarPedido(idPedido)

                // 2) notificar beneficiário
                try {
                    notifRepo.notificarDestinatario(
                        destinatarioId = idBeneficiario,
                        tipo = TipoNotificacao.PEDIDO_ATUALIZADO,
                        titulo = "Pedido cancelado",
                        conteudo = "O seu pedido (#$idPedido) foi cancelado pelo colaborador."
                    )
                } catch (_: Exception) {
                }

                // 3) refresh
                carregar(idBeneficiario)
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Erro ao cancelar pedido")
            }
        }
    }
}
