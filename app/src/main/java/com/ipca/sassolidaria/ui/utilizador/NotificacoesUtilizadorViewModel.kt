package com.ipca.sassolidaria.ui.utilizador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificacoesUtilizadorState(
    val notificacoes: List<Notificacao> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificacoesUtilizadorViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf(NotificacoesUtilizadorState())
        private set

    init {
        carregarNotificacoes()
    }

    fun carregarNotificacoes() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                uiState = uiState.copy(errorMessage = "Utilizador não autenticado", isLoading = false)
                return@launch
            }

            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val snapshot = db.collection(FirestoreCollections.NOTIFICACOES)
                    .whereEqualTo("destinatario", uid)
                    .get()
                    .await()

                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Notificacao::class.java)?.apply { id_notificacao = doc.id }
                }.sortedByDescending { it.data_envio }

                uiState = uiState.copy(notificacoes = lista, isLoading = false, errorMessage = null)

                // ✅ marcar todas como lidas
                marcarTodasComoLidas(uid)

            } catch (_: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Erro ao carregar notificações.")
            }
        }
    }

    private suspend fun marcarTodasComoLidas(uid: String) {
        val snap = db.collection(FirestoreCollections.NOTIFICACOES)
            .whereEqualTo("destinatario", uid)
            .whereEqualTo("lida", false)
            .get()
            .await()

        if (snap.isEmpty) return

        val batch = db.batch()
        snap.documents.forEach { batch.update(it.reference, "lida", true) }
        batch.commit().await()
    }
}
