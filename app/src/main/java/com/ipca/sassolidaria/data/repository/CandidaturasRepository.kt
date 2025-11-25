package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class CandidaturasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Devolve a candidatura mais recente de um dado utilizador
     * (ou null se não existir).
     */
    suspend fun getCandidaturaByUserId(userId: String): Candidatura? {
        if (userId.isBlank()) return null

        val snapshot = db.collection(FirestoreCollections.CANDIDATURAS)
            .whereEqualTo("id_utilizador", userId)
            .get()
            .await()

        // Aqui assumimos que um utilizador só tem (na prática) uma candidatura ativa.
        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(Candidatura::class.java)
    }
    suspend fun createCandidatura(candidatura: Candidatura) {
        val collection = db.collection(FirestoreCollections.CANDIDATURAS)
        val docRef = collection.document()
        candidatura.id_candidatura = docRef.id

        docRef.set(candidatura).await()
    }
}
