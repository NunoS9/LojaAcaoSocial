package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Apoio
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class ApoiosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getApoiosDoBeneficiario(idBeneficiario: String): List<Apoio> {
        val snap = db.collection(FirestoreCollections.APOIOS)
            .whereEqualTo("id_beneficiario", idBeneficiario)
            .get().await()

        return snap.documents.mapNotNull { d ->
            d.toObject(Apoio::class.java)?.copy(id_apoio = d.id)
        }
    }

    suspend fun criarApoio(idBeneficiario: String, mes: Int, idAgendamento: String) {
        val doc = db.collection(FirestoreCollections.APOIOS).document()
        val apoio = Apoio(
            id_apoio = doc.id,
            id_beneficiario = idBeneficiario,
            id_agendamento = idAgendamento,
            mes = mes
        )
        doc.set(apoio).await()
    }
}
