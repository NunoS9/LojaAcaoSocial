package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await

class NotificacoesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getNotificacoesDoUtilizador(userId: String): List<Notificacao> {
        val snapshot = db.collection(FirestoreCollections.NOTIFICACOES)
            .whereEqualTo("destinatario", userId) // <-- ajusta o nome do campo se for diferente
            .orderBy("data", Query.Direction.DESCENDING) // se tiveres campo data
            .get()
            .await()

        return snapshot.toObjects(Notificacao::class.java)
    }
}