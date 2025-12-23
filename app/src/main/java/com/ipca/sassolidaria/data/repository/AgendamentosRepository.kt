package com.ipca.sassolidaria.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.data.model.enums.Tipo
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await
import java.util.Date

class AgendamentosRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getAgendamentosDoBeneficiario(): List<Agendamento> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val benefSnapshot = db.collection(FirestoreCollections.BENEFICIARIOS)
            .whereEqualTo("id_utilizador", uid)
            .limit(1)
            .get()
            .await()

        val benefDoc = benefSnapshot.documents.firstOrNull() ?: return emptyList()
        val idBeneficiario = benefDoc.getString("id_beneficiario") ?: benefDoc.id

        val agSnap = db.collection(FirestoreCollections.AGENDAMENTOS)
            .whereEqualTo("id_beneficiario", idBeneficiario)
            .get()
            .await()

        return agSnap.documents.mapNotNull { doc ->
            doc.toObject(Agendamento::class.java)?.copy(id_agendamento = doc.id)
        }.distinctBy { it.id_agendamento }
            .sortedByDescending { it.data_agendada ?: Date(0) }
    }

    suspend fun atualizarEstadoAgendamento(idAgendamento: String, novoEstado: EstadoAgendamento) {
        db.collection(FirestoreCollections.AGENDAMENTOS)
            .document(idAgendamento)
            .update("estado", novoEstado.name)
            .await()
    }

    suspend fun criarAgendamento(
        idBeneficiario: String,
        data: Date,
        tipo: Tipo
    ): String {
        val docRef = db.collection(FirestoreCollections.AGENDAMENTOS).document()

        val agendamento = Agendamento(
            id_agendamento = docRef.id,
            id_beneficiario = idBeneficiario,
            data_agendada = data,
            estado = EstadoAgendamento.PENDENTE,
            tipo = tipo
        )

        docRef.set(agendamento).await()
        return docRef.id
    }

    suspend fun criarEntregaParaAgendamento(agendamento: Agendamento) {
        val entrega = Entrega(
            id_entrega = "",
            tipo = agendamento.tipo,
            id_beneficiario = agendamento.id_beneficiario,
            id_agendamento = agendamento.id_agendamento,
            id_colaborador = "",
            estado = EstadoEntrega.PENDENTE
        )

        val docRef = db.collection(FirestoreCollections.ENTREGAS).document()
        docRef.set(entrega.copy(id_entrega = docRef.id)).await()
    }

    fun observarAgendamentosDoBeneficiario(
        idBeneficiario: String,
        onChange: (List<Agendamento>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection(FirestoreCollections.AGENDAMENTOS)
            .whereEqualTo("id_beneficiario", idBeneficiario)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Agendamento::class.java)?.copy(id_agendamento = doc.id)
                }
                    .distinctBy { it.id_agendamento } // ✅ evita duplicados
                    .sortedByDescending { it.data_agendada ?: Date(0) } // ✅ ordena por data desc

                onChange(lista)
            }
    }

    suspend fun editarDataEReabrirAgendamento(
        idAgendamento: String,
        novaData: Date
    ) {
        db.collection(FirestoreCollections.AGENDAMENTOS)
            .document(idAgendamento)
            .update(
                mapOf(
                    "data_agendada" to novaData,
                    "estado" to EstadoAgendamento.PENDENTE.name
                )
            )
            .await()
    }

    // ✅ para ecrã de edição (buscar o agendamento atual)
    suspend fun getAgendamentoById(idAgendamento: String): Agendamento? {
        val snap = db.collection(FirestoreCollections.AGENDAMENTOS)
            .document(idAgendamento)
            .get()
            .await()

        val ag = snap.toObject(Agendamento::class.java) ?: return null
        return ag.copy(id_agendamento = snap.id)
    }
}
