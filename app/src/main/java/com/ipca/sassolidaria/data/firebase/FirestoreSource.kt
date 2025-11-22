package com.ipca.sassolidaria.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Utilizador
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class FirestoreSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getUtilizadorById(id: String): Utilizador? {
        val snapshot = db.collection(FirestoreCollections.UTILIZADORES)
            .document(id)
            .get()
            .await()

        return snapshot.toObject(Utilizador::class.java)
    }

    suspend fun saveUtilizador(utilizador: Utilizador) {
        if (utilizador.id_utilizador.isBlank()) return

        db.collection(FirestoreCollections.UTILIZADORES)
            .document(utilizador.id_utilizador)
            .set(utilizador)
            .await()
    }
}
