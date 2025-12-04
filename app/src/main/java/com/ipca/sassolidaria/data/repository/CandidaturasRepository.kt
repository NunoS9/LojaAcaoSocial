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

    // ====================================================
    //  ÁREA DO UTILIZADOR (CRIAR E VER A PRÓPRIA)
    // ====================================================

    /**
     * Cria uma nova candidatura.
     */
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

    /**
     * Busca a candidatura de um utilizador específico.
     */
    suspend fun getCandidaturaByUserId(userId: String): Candidatura? {
        if (userId.isBlank()) return null

        val snapshot = db.collection(FirestoreCollections.CANDIDATURAS)
            .whereEqualTo("id_utilizador", userId)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.toObject(Candidatura::class.java)
    }

    // ====================================================
    //  ÁREA DO COLABORADOR (GERIR, DETALHES E APROVAR)
    // ====================================================

    /**
     * Busca todas as candidaturas pendentes (EM_ANALISE).
     */
    suspend fun getCandidaturasPendentes(): List<Candidatura> {
        val snapshot = db.collection(FirestoreCollections.CANDIDATURAS)
            .whereEqualTo("estado", EstadoCandidatura.EM_ANALISE)
            .get()
            .await()
        return snapshot.toObjects(Candidatura::class.java)
    }

    /**
     * !!! ESTA É A FUNÇÃO QUE ESTAVA A FALTAR !!!
     * Busca uma candidatura específica pelo ID para mostrar nos detalhes.
     */
    suspend fun getCandidaturaById(id: String): Candidatura? {
        val doc = db.collection(FirestoreCollections.CANDIDATURAS)
            .document(id)
            .get()
            .await()
        return doc.toObject(Candidatura::class.java)
    }

    /**
     * Aprova a candidatura, muda o tipo de utilizador e cria o registo na tabela beneficiarios.
     */
    suspend fun aprovarCandidaturaEPromoverUser(candidatura: Candidatura) {
        val batch = db.batch()

        // 1. Atualizar estado da Candidatura
        val candidaturaRef = db.collection(FirestoreCollections.CANDIDATURAS)
            .document(candidatura.id_candidatura)
        batch.update(candidaturaRef, "estado", EstadoCandidatura.APROVADA)

        // 2. Promover o Utilizador a Beneficiário
        if (candidatura.id_utilizador.isNotBlank()) {
            val userRef = db.collection(FirestoreCollections.UTILIZADORES)
                .document(candidatura.id_utilizador)

            batch.update(userRef, "tipo", UserType.BENEFICIARIO)
            batch.update(userRef, "is_beneficiario", true)
        }

        // 3. Criar registo na tabela de Beneficiários
        val beneficiarioRef = db.collection(FirestoreCollections.BENEFICIARIOS).document()

        val novoBeneficiario = Beneficiario(
            id_beneficiario = beneficiarioRef.id,
            id_utilizador = candidatura.id_utilizador,
            nome = candidatura.nome,
            numero_estudante = candidatura.num_estudante.toString(),
            curso = candidatura.curso,
            grau_curso = candidatura.grau_curso,
            email = candidatura.email,
            contacto = candidatura.telemovel.toString()
        )

        batch.set(beneficiarioRef, novoBeneficiario)

        // Executar tudo
        batch.commit().await()
    }

    /**
     * Apenas rejeita a candidatura.
     */
    suspend fun recusarCandidatura(id: String) {
        db.collection(FirestoreCollections.CANDIDATURAS)
            .document(id)
            .update("estado", EstadoCandidatura.REJEITADA)
            .await()
    }
}