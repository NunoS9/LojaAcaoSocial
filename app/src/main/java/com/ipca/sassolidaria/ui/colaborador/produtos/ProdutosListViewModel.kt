package com.ipca.sassolidaria.ui.colaborador.produtos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.TipoProduto
import com.ipca.sassolidaria.data.repository.ProdutosRepository
import kotlinx.coroutines.launch
import java.util.Date

data class ProdutosListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val produtos: List<Produto> = emptyList()
)

class ProdutosListViewModel(
    private val repo: ProdutosRepository = ProdutosRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ProdutosListUiState())
        private set

    init {
        carregarProdutos()
    }

    fun carregarProdutos() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val lista = repo.getProdutos()

                // ordenar pela data de validade mais próxima
                val ordenada = lista.sortedBy { prod ->
                    prod.data_validade ?: Date(Long.MAX_VALUE)
                }

                uiState = uiState.copy(
                    isLoading = false,
                    produtos = ordenada
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar produtos"
                )
            }
        }
    }

    fun apagarProduto(id: String) {
        viewModelScope.launch {
            try {
                repo.deleteProduto(id)
                carregarProdutos()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Erro ao eliminar produto")
            }
        }
    }

    fun tipoToLabel(tipo: TipoProduto): String = when (tipo) {
        TipoProduto.HIGIENE     -> "Higiene"
        TipoProduto.LIMPEZA     -> "Limpeza"
        TipoProduto.ALIMENTACAO -> "Alimentação"
    }
}
