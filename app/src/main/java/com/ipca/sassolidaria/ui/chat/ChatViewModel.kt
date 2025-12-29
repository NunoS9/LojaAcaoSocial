package com.ipca.sassolidaria.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.chat.ChatMessage
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
) : ViewModel() {

    private fun currentUid(): String = auth.currentUser?.uid ?: ""

    fun createChatIfNeeded(chatId: String) {
        val docRef = db.collection("chats").document(chatId)
        docRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                docRef.set(
                    mapOf(
                        "beneficiarioId" to chatId,
                        "ativo" to true,
                        "createdAt" to System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun listenMessages(
        chatId: String,
        onUpdate: (List<ChatMessage>) -> Unit
    ) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull {
                        it.toObject(ChatMessage::class.java)
                    }
                    onUpdate(messages)
                }
            }
    }

    fun sendMessage(
        chatId: String,
        text: String,
        userType: UserType
    ) {
        val uid = currentUid()
        if (uid.isBlank() || text.isBlank()) return

        viewModelScope.launch {
            try {
                val msgRef = db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document()

                val lidaColab = (userType == UserType.COLABORADOR)
                val lidaBenef = (userType == UserType.BENEFICIARIO)

                val msg = ChatMessage(
                    id = msgRef.id,
                    senderId = uid,
                    senderTipo = userType.name,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    lidaPeloColaborador = lidaColab,
                    lidaPeloBeneficiario = lidaBenef
                )

                msgRef.set(msg).await()

                // ✅ Buscar nome do beneficiário (users/{chatId})
                val benefUserDoc = db.collection(FirestoreCollections.UTILIZADORES)
                    .document(chatId)
                    .get()
                    .await()
                val nomeBenef = benefUserDoc.getString("nome") ?: "Beneficiário"

                // ✅ Se precisar enviar notificação para beneficiário:
                //     destinatario tem de ser id_beneficiario (porque a tua view filtra por id_beneficiario)
                suspend fun getIdBeneficiarioByUserUid(userUid: String): String? {
                    val snap = db.collection(FirestoreCollections.BENEFICIARIOS)
                        .whereEqualTo("id_utilizador", userUid)
                        .limit(1)
                        .get()
                        .await()

                    val doc = snap.documents.firstOrNull() ?: return null
                    return doc.getString("id_beneficiario")?.takeIf { it.isNotBlank() } ?: doc.id
                }

                try {
                    when (userType) {
                        UserType.COLABORADOR -> {
                            val idBeneficiario = getIdBeneficiarioByUserUid(chatId)
                            if (!idBeneficiario.isNullOrBlank()) {
                                notifRepo.notificarDestinatario(
                                    destinatarioId = idBeneficiario,
                                    tipo = TipoNotificacao.GERAL,
                                    titulo = "Nova mensagem de suporte",
                                    conteudo = "Recebeu uma nova mensagem do colaborador."
                                )
                            }
                        }

                        UserType.BENEFICIARIO -> {
                            // nome do beneficiário = o próprio user logado (uid)
                            val myUserDoc = db.collection(FirestoreCollections.UTILIZADORES)
                                .document(uid)
                                .get()
                                .await()
                            val meuNome = myUserDoc.getString("nome") ?: "Beneficiário"

                            notifRepo.notificarTodosColaboradores(
                                tipo = TipoNotificacao.GERAL,
                                titulo = "Nova mensagem de suporte",
                                conteudo = "Nova mensagem de $meuNome no chat."
                            )
                        }

                        else -> Unit
                    }
                } catch (_: Exception) {
                    // não falha o envio da mensagem
                }

            } catch (_: Exception) {
                // se quiseres, mete state de erro
            }
        }
    }

    fun marcarMensagensComoLidasColaborador(chatId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("senderTipo", UserType.BENEFICIARIO.name)
            .whereEqualTo("lidaPeloColaborador", false)
            .get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { doc ->
                    doc.reference.update("lidaPeloColaborador", true)
                }
            }
    }

    fun marcarMensagensComoLidasBeneficiario(chatId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("senderTipo", UserType.COLABORADOR.name)
            .whereEqualTo("lidaPeloBeneficiario", false)
            .get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { doc ->
                    doc.reference.update("lidaPeloBeneficiario", true)
                }
            }
    }
}
