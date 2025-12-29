package com.ipca.sassolidaria.ui.beneficiario

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Beneficiario
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class NotificacoesBeneficiarioState(
    val notificacoes: List<Notificacao> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificacoesBeneficiarioViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf(NotificacoesBeneficiarioState())
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
                val benSnap = db.collection(FirestoreCollections.BENEFICIARIOS)
                    .whereEqualTo("id_utilizador", uid)
                    .limit(1)
                    .get()
                    .await()

                val benDoc = benSnap.documents.firstOrNull()
                if (benDoc == null) {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Beneficiário não encontrado.")
                    return@launch
                }

                val ben = benDoc.toObject(Beneficiario::class.java)
                val idBeneficiario = ben?.id_beneficiario?.takeIf { it.isNotBlank() } ?: benDoc.id

                val notifSnap = db.collection(FirestoreCollections.NOTIFICACOES)
                    .whereEqualTo("destinatario", idBeneficiario)
                    .get()
                    .await()

                val lista = notifSnap.documents.mapNotNull { doc ->
                    doc.toObject(Notificacao::class.java)?.apply { id_notificacao = doc.id }
                }.sortedByDescending { it.data_envio }

                uiState = uiState.copy(notificacoes = lista, isLoading = false, errorMessage = null)

                // ✅ marcar todas como lidas (as que estavam false)
                marcarTodasComoLidas(idBeneficiario)

            } catch (_: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Erro ao carregar notificações.")
            }
        }
    }

    private suspend fun marcarTodasComoLidas(idBeneficiario: String) {
        val snap = db.collection(FirestoreCollections.NOTIFICACOES)
            .whereEqualTo("destinatario", idBeneficiario)
            .whereEqualTo("lida", false)
            .get()
            .await()

        if (snap.isEmpty) return

        val batch = db.batch()
        snap.documents.forEach { batch.update(it.reference, "lida", true) }
        batch.commit().await()
    }
}
