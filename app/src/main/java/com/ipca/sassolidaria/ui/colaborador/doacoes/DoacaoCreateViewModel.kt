package com.ipca.sassolidaria.ui.colaborador.doacoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Doacao
import com.ipca.sassolidaria.data.model.Doador
import com.ipca.sassolidaria.data.model.ItemDoacao
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class DoacaoCreateViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _itens = MutableStateFlow<List<ItemDoacao>>(emptyList())
    val itens: StateFlow<List<ItemDoacao>> = _itens

    /* ---------------------------------------------------------
       LIMPAR LISTA DE ITENS APÓS REGISTO
    ---------------------------------------------------------- */
    fun clearItems() {
        _itens.value = emptyList()
    }

    /* ---------------------------------------------------------
       ADICIONAR ITEM
    ---------------------------------------------------------- */
    fun addItem(item: ItemDoacao) {
        val lista = _itens.value.toMutableList()
        item.id_item_doacao = UUID.randomUUID().toString()
        lista.add(item)
        _itens.value = lista
    }

    /* ---------------------------------------------------------
       EDITAR ITEM
    ---------------------------------------------------------- */
    fun updateItem(oldItem: ItemDoacao, newItem: ItemDoacao) {
        val lista = _itens.value.toMutableList()
        val index = lista.indexOfFirst { it.id_item_doacao == oldItem.id_item_doacao }
        if (index != -1) {
            newItem.id_item_doacao = oldItem.id_item_doacao
            lista[index] = newItem
            _itens.value = lista
        }
    }

    /* ---------------------------------------------------------
       REMOVER ITEM
    ---------------------------------------------------------- */
    fun removeItem(item: ItemDoacao) {
        _itens.value = _itens.value.filter { it.id_item_doacao != item.id_item_doacao }
    }

    /* ---------------------------------------------------------
       CRIAR DOAÇÃO COMPLETA
    ---------------------------------------------------------- */
    fun criarDoacao(
        nomeDoador: String,
        contacto: String,
        dataDoacao: Date,
        descricao: String,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {

            // 1 ➜ Criar DOADOR
            val idDoador = db.collection(FirestoreCollections.DOADORES).document().id

            val doador = Doador(
                id_doador = idDoador,
                nome = nomeDoador,
                contacto = contacto
            )

            db.collection(FirestoreCollections.DOADORES)
                .document(idDoador)
                .set(doador)
                .addOnFailureListener { onError() }
                .addOnSuccessListener {

                    // 2 ➜ Criar DOAÇÃO
                    val idDoacao = db.collection(FirestoreCollections.DOACOES).document().id

                    val doacao = Doacao(
                        id_doacao = idDoacao,
                        id_doador = idDoador,
                        id_colaborador = FirebaseAuth.getInstance().uid ?: "",
                        data_doacao = dataDoacao,
                        descricao = descricao
                    )

                    db.collection(FirestoreCollections.DOACOES)
                        .document(idDoacao)
                        .set(doacao)
                        .addOnFailureListener { onError() }
                        .addOnSuccessListener {

                            // 3 ➜ Criar ITENS DA DOAÇÃO
                            _itens.value.forEach { item ->

                                val idItem = db.collection(FirestoreCollections.ITENS_DOACAO).document().id

                                val finalItem = item.copy(
                                    id_item_doacao = idItem,
                                    id_doacao = idDoacao
                                )

                                db.collection(FirestoreCollections.ITENS_DOACAO)
                                    .document(idItem)
                                    .set(finalItem)

                                // 4 ➜ Atualizar tabela PRODUTOS
                                atualizarOuCriarProduto(finalItem)
                            }

                            // 🔥 5 ➜ LIMPAR ITENS APÓS FINALIZAR (RESOLVE O TEU BUG!)
                            clearItems()

                            // 6 ➜ Navegar para o detalhe
                            onSuccess(idDoacao)
                        }
                }
        }
    }

    /* ---------------------------------------------------------
       ATUALIZAR OU CRIAR PRODUTO NO STOCK
    ---------------------------------------------------------- */
    private fun atualizarOuCriarProduto(item: ItemDoacao) {

        val produtosRef = db.collection(FirestoreCollections.PRODUTOS)

        produtosRef
            .whereEqualTo("nome", item.nome)
            .get()
            .addOnSuccessListener { result ->

                if (!result.isEmpty) {
                    // Produto existe → atualizar stock
                    val doc = result.documents[0]

                    val atual = doc.getLong("quantidade")?.toInt() ?: 0
                    val atualDisp = doc.getLong("quantidade_disponivel")?.toInt() ?: 0

                    val novosDados = mapOf(
                        "quantidade" to (atual + item.quantidade),
                        "quantidade_disponivel" to (atualDisp + item.quantidade),
                        "tipo_produto" to item.tipo_produto,
                        "data_validade" to item.data_validade
                    )

                    produtosRef.document(doc.id).update(novosDados)

                } else {
                    // Produto novo → criar
                    val idProduto = produtosRef.document().id

                    val novoProduto = mapOf(
                        "id_produto" to idProduto,
                        "nome" to item.nome,
                        "tipo_produto" to item.tipo_produto,
                        "quantidade" to item.quantidade,
                        "quantidade_disponivel" to item.quantidade,
                        "data_validade" to item.data_validade
                    )

                    produtosRef.document(idProduto).set(novoProduto)
                }
            }
    }
}
