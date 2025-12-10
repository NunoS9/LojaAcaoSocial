package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class EntregasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun criarEntrega(entrega: Entrega) {
        val docRef = db.collection(FirestoreCollections.ENTREGAS).document()
        val data = entrega.copy(id_entrega = docRef.id)
        docRef.set(data).await()
    }
}
