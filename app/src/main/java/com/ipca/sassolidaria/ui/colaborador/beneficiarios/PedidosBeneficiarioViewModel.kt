package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.data.repository.PedidosRepository
import kotlinx.coroutines.launch
import java.util.Date

data class PedidosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pedidos: List<Pedido> = emptyList()
)

class PedidosBeneficiarioViewModel(
    private val repo: PedidosRepository = PedidosRepository()
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
                repo.cancelarPedido(idPedido)
                carregar(idBeneficiario)
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Erro ao cancelar pedido")
            }
        }
    }
}
