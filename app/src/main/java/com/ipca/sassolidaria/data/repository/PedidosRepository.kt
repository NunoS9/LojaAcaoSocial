package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await
import com.ipca.sassolidaria.data.model.Pedido
import com.ipca.sassolidaria.data.model.enums.EstadoPedido
class PedidosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getPedidosDoBeneficiario(idBeneficiario: String): List<Pedido> {
        val snap = db.collection(FirestoreCollections.PEDIDOS)
            .whereEqualTo("id_beneficiario", idBeneficiario)
            .get().await()

        return snap.documents.mapNotNull { d ->
            d.toObject(Pedido::class.java)?.copy(id_pedido = d.id)
        }
    }

    suspend fun cancelarPedido(idPedido: String) {
        db.collection(FirestoreCollections.PEDIDOS)
            .document(idPedido)
            .update("estado", EstadoPedido.CANCELADO.name)
            .await()
    }

    suspend fun aceitarPedidoEAssociarAgendamento(idPedido: String, idAgendamento: String) {
        db.collection(FirestoreCollections.PEDIDOS)
            .document(idPedido)
            .update(
                mapOf(
                    "estado" to EstadoPedido.ACEITE.name,
                    "id_agendamento" to idAgendamento
                )
            )
            .await()
    }
}