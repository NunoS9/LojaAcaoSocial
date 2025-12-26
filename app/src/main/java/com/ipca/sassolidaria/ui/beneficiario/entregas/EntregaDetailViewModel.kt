package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.ItemEntrega
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EntregaDetalhesViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    var detalhesState: ResultState<List<String>> by mutableStateOf(ResultState.Idle)
        private set

    // 🚀 cache produtoId -> nome
    private val produtoNomeCache = mutableMapOf<String, String>()

    fun carregarItensEntrega(entregaId: String) {
        detalhesState = ResultState.Loading

        viewModelScope.launch {
            try {
                // 1) localizar doc da entrega
                val entregaDocDirect = db.collection(FirestoreCollections.ENTREGAS)
                    .document(entregaId)
                    .get()
                    .await()

                val entregaDoc = if (entregaDocDirect.exists()) {
                    entregaDocDirect
                } else {
                    val snap = db.collection(FirestoreCollections.ENTREGAS)
                        .whereEqualTo("id_entrega", entregaId)
                        .limit(1)
                        .get()
                        .await()
                    snap.documents.firstOrNull()
                }

                if (entregaDoc == null) {
                    detalhesState = ResultState.Error("Entrega não encontrada.")
                    return@launch
                }

                val entrega = entregaDoc.toObject(Entrega::class.java)

                val possibleEntregaIds = mutableListOf<String>()
                possibleEntregaIds.add(entregaDoc.id)
                entrega?.id_entrega?.takeIf { it.isNotBlank() }?.let { possibleEntregaIds.add(it) }

                // 2) itens_entrega
                val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                    .whereIn("id_entrega", possibleEntregaIds.distinct())
                    .get()
                    .await()

                if (itensSnap.isEmpty) {
                    detalhesState = ResultState.Error("Nenhum item associado a esta entrega.")
                    return@launch
                }

                val itens = itensSnap.documents.mapNotNull { it.toObject(ItemEntrega::class.java) }
                val idsProdutos = itens.mapNotNull { it.id_produto.takeIf { id -> id.isNotBlank() } }
                    .distinct()

                // 3) buscar nomes dos produtos em batch (só os que não estão em cache)
                val missing = idsProdutos.filterNot { produtoNomeCache.containsKey(it) }
                if (missing.isNotEmpty()) {
                    val chunks = missing.chunked(10)

                    for (chunk in chunks) {
                        val prodSnap = db.collection(FirestoreCollections.PRODUTOS)
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        prodSnap.documents.forEach { d ->
                            val p = d.toObject(Produto::class.java)
                            produtoNomeCache[d.id] = p?.nome ?: "Produto"
                        }
                    }
                }

                // 4) montar linhas
                val linhas = itens.map { item ->
                    val nome = produtoNomeCache[item.id_produto] ?: "Produto"
                    "$nome (${item.quantidade})"
                }

                detalhesState = ResultState.Success(linhas)

            } catch (e: Exception) {
                detalhesState = ResultState.Error(e.message ?: "Erro ao carregar itens da entrega.")
            }
        }
    }

    fun limparDetalhes() {
        detalhesState = ResultState.Idle
    }
}
