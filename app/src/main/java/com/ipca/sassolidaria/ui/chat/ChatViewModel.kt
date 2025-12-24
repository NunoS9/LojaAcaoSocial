package com.ipca.sassolidaria.ui.chat

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.data.model.chat.ChatMessage

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun currentUid(): String =
        auth.currentUser?.uid ?: ""

    /* ---------- CHAT ---------- */

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

    fun closeChat(chatId: String) {
        db.collection("chats")
            .document(chatId)
            .update("ativo", false)
    }

    /* ---------- MESSAGES ---------- */

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
        if (uid.isBlank()) return

        val msg = mapOf(
            "senderId" to uid,
            "senderTipo" to userType.name,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "lidaPeloColaborador" to false
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(msg)
    }

    /* ---------- LER MENSAGENS (NOVO) ---------- */

    fun marcarMensagensComoLidas(chatId: String) {
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
}
