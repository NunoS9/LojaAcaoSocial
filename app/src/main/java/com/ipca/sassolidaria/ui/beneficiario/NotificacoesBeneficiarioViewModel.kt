package com.ipca.sassolidaria.ui.beneficiario

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.utils.FirestoreCollections

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

    private fun carregarNotificacoes() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            uiState = uiState.copy(
                errorMessage = "Utilizador não autenticado",
                isLoading = false
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        db.collection(FirestoreCollections.NOTIFICACOES)
            .whereEqualTo("destinatario", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.toObjects(Notificacao::class.java)
                uiState = uiState.copy(
                    notificacoes = lista,
                    isLoading = false,
                    errorMessage = null
                )
            }
            .addOnFailureListener { e ->
                Log.e("NotifBenefVM", "Erro ao carregar notificações", e)
                uiState = uiState.copy(
                    errorMessage = "Erro ao carregar notificações",
                    isLoading = false
                )
            }
    }
}
