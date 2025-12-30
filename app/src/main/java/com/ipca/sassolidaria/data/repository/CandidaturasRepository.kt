package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.model.enums.EstadoCandidatura
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class CandidaturasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createCandidatura(candidatura: Candidatura) {
        val collection = db.collection(FirestoreCollections.CANDIDATURAS)

        val docRef = if (candidatura.id_candidatura.isBlank()) {
            collection.document()
        } else {
            collection.document(candidatura.id_candidatura)
        }

        candidatura.id_candidatura = docRef.id
        docRef.set(candidatura).await()
    }

    suspend fun getCandidaturaByUserId(userId: String): Candidatura? {
        if (userId.isBlank()) return null

        val snapshot = db.collection(FirestoreCollections.CANDIDATURAS)
            .whereEqualTo("id_utilizador", userId)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(Candidatura::class.java)
    }

    suspend fun getCandidaturasPendentes(): List<Candidatura> {
        val snapshot = db.collection(FirestoreCollections.CANDIDATURAS)
            .whereEqualTo("estado", EstadoCandidatura.EM_ANALISE)
            .get()
            .await()
        return snapshot.toObjects(Candidatura::class.java)
    }

    suspend fun getCandidaturaById(id: String): Candidatura? {
        val doc = db.collection(FirestoreCollections.CANDIDATURAS)
            .document(id)
            .get()
            .await()
        return doc.toObject(Candidatura::class.java)
    }

    suspend fun aprovarCandidaturaEPromoverUser(candidatura: Candidatura): String {
        val batch = db.batch()

        // 1) update candidatura
        val candRef = db.collection(FirestoreCollections.CANDIDATURAS)
            .document(candidatura.id_candidatura)

        batch.update(candRef, "estado", EstadoCandidatura.APROVADA.name)

        // 2) criar beneficiário
        val benRef = db.collection(FirestoreCollections.BENEFICIARIOS).document()
        val idBeneficiario = benRef.id

        val beneficiario = Beneficiario(
            id_beneficiario = idBeneficiario,
            id_utilizador = candidatura.id_utilizador,
            nome = candidatura.nome,
            numero_estudante = candidatura.num_estudante.toString(),
            curso = candidatura.curso,
            grau_curso = candidatura.grau_curso,
            email = candidatura.email,
            contacto = candidatura.telemovel.toString()
        )

        batch.set(benRef, beneficiario)

        // 3) promover user
        val userRef = db.collection(FirestoreCollections.UTILIZADORES)
            .document(candidatura.id_utilizador)

        batch.update(userRef, mapOf(
            "tipo" to UserType.BENEFICIARIO.name,
            "is_beneficiario" to true
        ))

        batch.commit().await()
        return idBeneficiario
    }
    suspend fun recusarCandidatura(id: String) {
        db.collection(FirestoreCollections.CANDIDATURAS)
            .document(id)
            .update("estado", EstadoCandidatura.REJEITADA)
            .await()
    }
}