package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
}
