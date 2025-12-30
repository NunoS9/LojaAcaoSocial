package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.sassolidaria.data.model.PreferenciaAgendamento
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class PreferenciasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getUltimaPreferencia(idDoc: String): PreferenciaAgendamento? {
        val snap = db.collection(FirestoreCollections.PREFERENCIAS_AGENDAMENTO)
            .document(idDoc)
            .collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        val d = snap.documents.firstOrNull() ?: return null

        val dias = d.get("dias") as? List<*> ?: emptyList<Any>()
        val primeiro = dias.firstOrNull() as? String ?: ""

        val diaSemana = when (primeiro) {
            "Domingo" -> 1
            "Segunda" -> 2
            "Terça" -> 3
            "Quarta" -> 4
            "Quinta" -> 5
            "Sexta" -> 6
            "Sábado" -> 7
            else -> 0
        }

        return PreferenciaAgendamento(
            id_preferencia = d.id,
            id_beneficiario = d.getString("id_beneficiario") ?: "",
            dia_semana = diaSemana,
            hora_inicio = d.getString("horaInicio") ?: "",
            hora_fim = d.getString("horaFim") ?: "",
            observacoes = d.getString("observacoes") ?: ""
        )
    }
}
