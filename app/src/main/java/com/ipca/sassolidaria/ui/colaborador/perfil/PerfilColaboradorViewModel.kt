package com.ipca.sassolidaria.ui.colaborador.perfil

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilColaboradorViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilColaboradorState())
    val uiState: StateFlow<PerfilColaboradorState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                _uiState.value = _uiState.value.copy(
                    nome = doc.getString("nome") ?: "",
                    email = doc.getString("email") ?: ""
                )
            }
    }

    fun updateName(
        newName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (newName.isBlank()) {
            onError("O nome não pode estar vazio.")
            return
        }

        val uid = auth.currentUser?.uid ?: return onError("Sessão expirada.")

        firestore.collection("users")
            .document(uid)
            .update("nome", newName)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(nome = newName)
                onSuccess()
            }
            .addOnFailureListener {
                onError("Erro ao atualizar nome.")
            }
    }
}

data class PerfilColaboradorState(
    val nome: String = "",
    val email: String = ""
)
