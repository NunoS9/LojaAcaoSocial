package com.ipca.sassolidaria.ui.colaborador.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _uiState.value = _uiState.value.copy(
                    nome = doc.getString("nome") ?: "",
                    email = doc.getString("email") ?: ""
                )
            }
    }

    fun editUserData(
        newName: String,
        newEmail: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {

        if (newName.isBlank() || newEmail.isBlank()) {
            onError()
            return
        }

        val uid = auth.currentUser!!.uid

        viewModelScope.launch {
            try {
                auth.currentUser!!.updateEmail(newEmail)
                    .addOnSuccessListener {

                        firestore.collection("users").document(uid)
                            .update(mapOf("nome" to newName, "email" to newEmail))
                            .addOnSuccessListener {
                                _uiState.value = _uiState.value.copy(
                                    nome = newName,
                                    email = newEmail
                                )
                                onSuccess()
                            }
                            .addOnFailureListener { onError() }

                    }
                    .addOnFailureListener { onError() }

            } catch (e: Exception) {
                onError()
            }
        }
    }

    fun updatePassword(
        newPassword: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {

        if (newPassword.isBlank()) {
            onError()
            return
        }

        auth.currentUser!!.updatePassword(newPassword)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError() }
    }
}

data class PerfilColaboradorState(
    val nome: String = "",
    val email: String = ""
)
