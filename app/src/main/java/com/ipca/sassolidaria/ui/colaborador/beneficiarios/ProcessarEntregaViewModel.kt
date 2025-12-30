package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.ItemEntrega
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProcessarEntregaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val produtos: List<Produto> = emptyList(),
    val selecoesAtuais: Map<String, Int> = emptyMap()
)

class ProcessarEntregaViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    var uiState by mutableStateOf(ProcessarEntregaUiState())
        private set

    fun carregarTudo(idEntrega: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                // 1) Produtos
                val prodSnap = db.collection(FirestoreCollections.PRODUTOS)
                    .get()
                    .await()

                val produtos = prodSnap.documents.mapNotNull { doc ->
                    doc.toObject(Produto::class.java)?.copy(id_produto = doc.id)
                }

                // 2) Itens já existentes da entrega
                val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                    .whereEqualTo("id_entrega", idEntrega)
                    .get()
                    .await()

                val mapa = mutableMapOf<String, Int>()
                itensSnap.documents.forEach { d ->
                    val item = d.toObject(ItemEntrega::class.java)
                    if (item != null && item.id_produto.isNotBlank()) {
                        mapa[item.id_produto] = (mapa[item.id_produto] ?: 0) + item.quantidade
                    }
                }

                uiState = uiState.copy(
                    isLoading = false,
                    produtos = produtos,
                    selecoesAtuais = mapa,
                    error = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar produtos/itens")
            }
        }
    }
    fun processarEntrega(
        idEntrega: String,
        selecoes: Map<String, Int>,
        onOk: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val filtradas = selecoes.filterValues { it > 0 }
                if (filtradas.isEmpty()) {
                    uiState = uiState.copy(isLoading = false, error = "Seleciona pelo menos 1 produto.")
                    return@launch
                }

                // validar stock
                val prodMap = uiState.produtos.associateBy { it.id_produto }
                for ((idProd, qtd) in filtradas) {
                    val p = prodMap[idProd]
                    val stock = p?.quantidade_disponivel ?: 0
                    if (qtd > stock) {
                        uiState = uiState.copy(isLoading = false, error = "Quantidade excede stock: ${p?.nome ?: idProd}")
                        return@launch
                    }
                }

                // apagar itens antigos
                val antigos = db.collection(FirestoreCollections.ITENS_ENTREGA)
                    .whereEqualTo("id_entrega", idEntrega)
                    .get()
                    .await()

                antigos.documents.forEach { it.reference.delete().await() }

                // criar novos itens
                for ((idProd, qtd) in filtradas) {
                    val docRef = db.collection(FirestoreCollections.ITENS_ENTREGA).document()
                    val item = ItemEntrega(
                        id_item_entrega = docRef.id,
                        id_entrega = idEntrega,
                        id_produto = idProd,
                        quantidade = qtd
                    )
                    docRef.set(item).await()
                }

                // estado -> PRONTA
                db.collection(FirestoreCollections.ENTREGAS)
                    .document(idEntrega)
                    .update("estado", EstadoEntrega.PRONTA.name)
                    .await()

                uiState = uiState.copy(isLoading = false, selecoesAtuais = filtradas, error = null)
                onOk()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao processar entrega")
            }
        }
    }
}
