package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega

class EntregasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun criarEntrega(entrega: Entrega) {
        val docRef = db.collection(FirestoreCollections.ENTREGAS).document()
        val data = entrega.copy(id_entrega = docRef.id)
        docRef.set(data).await()
    }

    fun observarEntregasDoBeneficiario(
        idBeneficiario: String,
        onChange: (List<Entrega>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection(FirestoreCollections.ENTREGAS)
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

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Entrega::class.java)?.copy(id_entrega = doc.id)
                }

                onChange(list)
            }
    }

    suspend fun atualizarEstadoEntrega(idEntrega: String, estado: EstadoEntrega) {
        db.collection(FirestoreCollections.ENTREGAS)
            .document(idEntrega)
            .update("estado", estado.name)
            .await()
    }
}
