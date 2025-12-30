package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.ItemEntrega
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class ProdutosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getProdutos(): List<Produto> {
        val snap = db.collection(FirestoreCollections.PRODUTOS)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            doc.toObject(Produto::class.java)?.copy(id_produto = doc.id)
        }
    }

    suspend fun getProduto(id: String): Produto? {
        val snap = db.collection(FirestoreCollections.PRODUTOS)
            .document(id)
            .get()
            .await()

        return snap.toObject(Produto::class.java)?.copy(id_produto = snap.id)
    }

    suspend fun addProduto(produto: Produto) {
        val docRef = db.collection(FirestoreCollections.PRODUTOS)
            .document()

        val data = produto.copy(id_produto = docRef.id)
        docRef.set(data).await()
    }

    suspend fun updateProduto(produto: Produto) {
        if (produto.id_produto.isBlank()) return

        db.collection(FirestoreCollections.PRODUTOS)
            .document(produto.id_produto)
            .set(produto)
            .await()
    }

    suspend fun deleteProduto(id: String) {
        db.collection(FirestoreCollections.PRODUTOS)
            .document(id)
            .delete()
            .await()
    }

    suspend fun debitarStock(itens: List<ItemEntrega>) {
        if (itens.isEmpty()) return

        db.runTransaction { tx ->

            // agrupar por produto (se vier repetido)
            val porProduto = itens.groupBy { it.id_produto }
                .mapValues { (_, list) -> list.sumOf { it.quantidade } }

            // ---------- READS ----------
            val refs = porProduto.keys.associateWith { idProd ->
                db.collection(FirestoreCollections.PRODUTOS).document(idProd)
            }
            val snaps = refs.mapValues { (_, ref) -> tx.get(ref) }

            // validar
            porProduto.forEach { (idProd, qtd) ->
                val snap = snaps[idProd]!!
                val atual = (snap.getLong("quantidade_disponivel") ?: 0L).toInt()
                val novo = atual - qtd
                if (novo < 0) throw IllegalStateException("Stock insuficiente para o produto $idProd")
            }

            // Writes
            porProduto.forEach { (idProd, qtd) ->
                val ref = refs[idProd]!!
                val snap = snaps[idProd]!!
                val atual = (snap.getLong("quantidade_disponivel") ?: 0L).toInt()
                val novo = atual - qtd
                tx.update(ref, "quantidade_disponivel", novo)
            }

            null
        }.await()
    }
}
