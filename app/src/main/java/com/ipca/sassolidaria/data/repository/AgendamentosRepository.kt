package com.ipca.sassolidaria.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class AgendamentosRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * 1) descobre o id_beneficiario do utilizador logado (pela coleção "beneficiarios")
     * 2) devolve os agendamentos desse beneficiário
     */
    suspend fun getAgendamentosDoBeneficiario(): List<Agendamento> {

        // 1) UID do utilizador autenticado
        val uid = auth.currentUser?.uid ?: return emptyList()

        // 2) Procurar o beneficiário com esse id_utilizador
        val benefSnapshot = db.collection(FirestoreCollections.BENEFICIARIOS)
            .whereEqualTo("id_utilizador", uid)
            .limit(1)
            .get()
            .await()

        val benefDoc = benefSnapshot.documents.firstOrNull() ?: return emptyList()

        // tenta usar o campo id_beneficiario; se não existir, usa o próprio id do doc
        val idBeneficiario = benefDoc.getString("id_beneficiario") ?: benefDoc.id

        // 3) TENTAR buscar agendamentos com whereEqualTo
        val querySnapshot = db.collection(FirestoreCollections.AGENDAMENTOS)
            .whereEqualTo("id_beneficiario", idBeneficiario)
            .get()
            .await()

        // 4) Se a query vier vazia, fazer fallback: buscar todos e filtrar em memória
        val docs = if (querySnapshot.isEmpty) {
            db.collection(FirestoreCollections.AGENDAMENTOS)
                .get()
                .await()
                .documents
        } else {
            querySnapshot.documents
        }

        return docs.mapNotNull { doc ->
            doc.toObject(Agendamento::class.java)?.copy(
                id_agendamento = doc.id
            )
        }.filter { ag ->
            ag.id_beneficiario == idBeneficiario
        }
    }

    suspend fun atualizarEstadoAgendamento(
        idAgendamento: String,
        novoEstado: EstadoAgendamento
    ) {
        db.collection(FirestoreCollections.AGENDAMENTOS)
            .document(idAgendamento)
            .update("estado", novoEstado.name)
            .await()
    }

    /**
     * (opcional) se quiseres que o repo próprio crie a entrega
     */
    suspend fun criarEntregaParaAgendamento(
        agendamento: Agendamento
    ) {
        val entrega = Entrega(
            id_entrega = "",
            tipo = agendamento.tipo,
            id_beneficiario = agendamento.id_beneficiario,
            id_agendamento = agendamento.id_agendamento,
            id_colaborador = "", // preenchido depois se quiseres
            estado = EstadoEntrega.PENDENTE
        )

        val docRef = db.collection(FirestoreCollections.ENTREGAS)
            .document()

        docRef.set(entrega.copy(id_entrega = docRef.id)).await()
    }
}
