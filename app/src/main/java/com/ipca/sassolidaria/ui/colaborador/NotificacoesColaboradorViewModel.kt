package com.ipca.sassolidaria.ui.colaborador

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

data class NotificacoesColaboradorState(
    val notificacoes: List<Notificacao> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class NotificacoesColaboradorViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var uiState by mutableStateOf(NotificacoesColaboradorState())
        private set

    fun carregar() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Utilizador não autenticado.")
                return@launch
            }

            try {
                val snap = db.collection(FirestoreCollections.NOTIFICACOES)
                    .whereEqualTo("destinatario", uid)
                    .get()
                    .await()

                val lista = snap.documents.mapNotNull { doc ->
                    doc.toObject(Notificacao::class.java)?.apply {
                        // garantir id do doc
                        if (id_notificacao.isBlank()) id_notificacao = doc.id
                    }
                }.sortedByDescending { it.data_envio?.time ?: 0L }

                uiState = uiState.copy(notificacoes = lista, isLoading = false, errorMessage = null)

                // ✅ marcar como lidas (lida != true)
                val porLer = snap.documents.filter { it.getBoolean("lida") != true }
                if (porLer.isNotEmpty()) {
                    val batch = db.batch()
                    porLer.forEach { d ->
                        batch.update(d.reference, "lida", true)
                    }
                    batch.commit().await()
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erro ao carregar notificações."
                )
            }
        }
    }
}
