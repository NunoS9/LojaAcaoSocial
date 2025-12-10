package com.ipca.sassolidaria.ui.utilizador.perfil

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilUtilizadorViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUtilizadorState())
    val uiState: StateFlow<PerfilUtilizadorState> = _uiState

    init { loadUser() }

    private fun loadUser() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _uiState.value = PerfilUtilizadorState(
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
        val uid = auth.currentUser?.uid ?: return

        if (newName.isBlank()) {
            onError("O nome não pode estar vazio.")
            return
        }

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

    fun updatePassword(
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (newPassword.isBlank()) {
            onError("A nova password não pode estar vazia.")
            return
        }

        auth.currentUser!!
            .updatePassword(newPassword)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError("Erro ao alterar password") }
    }
}

data class PerfilUtilizadorState(
    val nome: String = "",
    val email: String = ""
)
