package com.ipca.sassolidaria.ui.colaborador.produtos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.TipoProduto
import com.ipca.sassolidaria.data.repository.ProdutosRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ProdutoFormUiState(
    val id: String? = null,
    val nome: String = "",
    val tipo: TipoProduto = TipoProduto.ALIMENTACAO,
    val quantidade: String = "",
    val validade: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ProdutoFormViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repo = ProdutosRepository()

    var uiState by mutableStateOf(ProdutoFormUiState())
        private set

    private val editingId: String? = savedStateHandle.get<String>("id")

    init {
        if (!editingId.isNullOrBlank()) {
            carregarProduto(editingId)
        }
    }

    private fun carregarProduto(id: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val prod = repo.getProduto(id)
                if (prod != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        id = prod.id_produto,
                        nome = prod.nome,
                        tipo = prod.tipo_produto,
                        quantidade = prod.quantidade_disponivel.toString(),
                        validade = prod.data_validade?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
                        } ?: ""
                    )
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Produto não encontrado"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar produto"
                )
            }
        }
    }

    fun onNomeChange(v: String) {
        uiState = uiState.copy(nome = v)
    }

    fun onQuantidadeChange(v: String) {
        uiState = uiState.copy(quantidade = v.filter { it.isDigit() })
    }

    fun onValidadeChange(v: String) {
        uiState = uiState.copy(validade = v)
    }

    fun onTipoChange(v: TipoProduto) {
        uiState = uiState.copy(tipo = v)
    }

    private fun parseDateOrNull(text: String): Date? {
        if (text.isBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(text)
        } catch (_: Exception) {
            null
        }
    }

    fun guardar(onFinished: () -> Unit) {
        val nome = uiState.nome.trim()
        val q = uiState.quantidade.toIntOrNull() ?: 0

        if (nome.isEmpty() || q <= 0) {
            uiState = uiState.copy(errorMessage = "Preencha nome e quantidade")
            return
        }

        val date = parseDateOrNull(uiState.validade)

        // não deixar datas de validade no passado
        if (date != null) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val hoje = cal.time

            if (date.before(hoje)) {
                uiState = uiState.copy(
                    errorMessage = "Data de validade não pode ser no passado"
                )
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val produto = Produto(
                    id_produto = uiState.id ?: "",
                    nome = nome,
                    tipo_produto = uiState.tipo,
                    quantidade = q,
                    quantidade_disponivel = q,
                    data_validade = date
                )

                if (uiState.id == null) {
                    repo.addProduto(produto)
                } else {
                    repo.updateProduto(produto)
                }

                uiState = uiState.copy(
                    isLoading = false,
                    successMessage = "Guardado com sucesso"
                )
                onFinished()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao guardar produto"
                )
            }
        }
    }


    fun eliminar(onFinished: () -> Unit) {
        val id = uiState.id ?: return
        viewModelScope.launch {
            try {
                repo.deleteProduto(id)
                onFinished()
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Erro ao eliminar produto")
            }
        }
    }
}
