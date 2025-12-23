package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.ItemEntrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class ItensEntregaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun limparItensDaEntrega(idEntrega: String) {
        val snap = db.collection(FirestoreCollections.ITENS_ENTREGA)
            .whereEqualTo("id_entrega", idEntrega)
            .get()
            .await()

        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun criarItensEntrega(idEntrega: String, itens: List<ItemEntrega>) {
        val batch = db.batch()
        itens.forEach { item ->
            val docRef = db.collection(FirestoreCollections.ITENS_ENTREGA).document()
            val toSave = item.copy(
                id_item_entrega = docRef.id,
                id_entrega = idEntrega
            )
            batch.set(docRef, toSave)
        }
        batch.commit().await()
    }

    suspend fun getItensDaEntrega(idEntrega: String): List<ItemEntrega> {
        val snap = db.collection(FirestoreCollections.ITENS_ENTREGA)
            .whereEqualTo("id_entrega", idEntrega)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            doc.toObject(ItemEntrega::class.java)?.copy(id_item_entrega = doc.id)
        }
    }
}
