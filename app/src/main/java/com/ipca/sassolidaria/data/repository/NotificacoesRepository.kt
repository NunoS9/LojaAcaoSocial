package com.ipca.sassolidaria.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.tasks.await
import java.util.Date

class NotificacoesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun notificarTodosColaboradores(
        tipo: TipoNotificacao,
        titulo: String,
        conteudo: String
    ) {
        val colaboradoresSnap = db.collection(FirestoreCollections.UTILIZADORES) // "users"
            .whereEqualTo("tipo", UserType.COLABORADOR.name)
            .get()
            .await()

        if (colaboradoresSnap.isEmpty) return

        val batch = db.batch()

        for (doc in colaboradoresSnap.documents) {
            val destinatarioId = doc.getString("id_utilizador") ?: doc.id

            val notifRef = db.collection(FirestoreCollections.NOTIFICACOES).document()

            val notif = Notificacao(
                id_notificacao = notifRef.id,
                destinatario = destinatarioId,
                tipo = tipo,
                titulo = titulo,
                conteudo = conteudo,
                data_envio = Date(),
                lida = false
            )

            batch.set(notifRef, notif)
        }

        batch.commit().await()
    }

    /**
     * ✅ Nova: notificar UM destinatário (pode ser uid do user OU id_beneficiario)
     */
    suspend fun notificarDestinatario(
        destinatarioId: String,
        tipo: TipoNotificacao,
        titulo: String,
        conteudo: String
    ) {
        val notifRef = db.collection(FirestoreCollections.NOTIFICACOES).document()

        val notif = Notificacao(
            id_notificacao = notifRef.id,
            destinatario = destinatarioId,
            tipo = tipo,
            titulo = titulo,
            conteudo = conteudo,
            data_envio = Date(),
            lida = false
        )

        notifRef.set(notif).await()
    }
}
