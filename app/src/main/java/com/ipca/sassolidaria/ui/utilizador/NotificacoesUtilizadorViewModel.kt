package com.ipca.sassolidaria.ui.utilizador

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.utils.FirestoreCollections

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

        db.collection(FirestoreCollections.NOTIFICACOES)   // "notificacoes"
            .whereEqualTo("destinatario", uid)            // campo que tens no Firestore
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
                Log.e("NotificacoesVM", "Erro ao carregar notificações", e)
                uiState = uiState.copy(
                    errorMessage = "Erro ao carregar notificações",
                    isLoading = false
                )
            }
    }
}
