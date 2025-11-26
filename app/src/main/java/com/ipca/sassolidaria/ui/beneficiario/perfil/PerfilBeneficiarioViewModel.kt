package com.ipca.sassolidaria.ui.beneficiario.perfil

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PerfilBeneficiarioState(
    val nome: String = "",
    val email: String = "",
    val id_beneficiario: String = "",
    val diasSelecionados: List<String> = emptyList(),
    val horaInicio: String = "",
    val horaFim: String = "",
    val observacoes: String = "",
    val success: String = "",
    val error: String = ""
)

class PerfilBeneficiarioViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilBeneficiarioState())
    val uiState: StateFlow<PerfilBeneficiarioState> = _uiState

    init {
        val uid = auth.currentUser?.uid ?: ""
        _uiState.value = _uiState.value.copy(id_beneficiario = uid)
        loadUser()
    }

    // CARREGAR DADOS DO UTILIZADOR
    private fun loadUser() {
        val uid = auth.currentUser?.uid ?: return

        db.collection(FirestoreCollections.UTILIZADORES)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                _uiState.value = _uiState.value.copy(
                    nome = doc.getString("nome") ?: "",
                    email = doc.getString("email") ?: "",
                    id_beneficiario = uid
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

        val uid = auth.currentUser?.uid ?: return onError("Erro.")

        db.collection(FirestoreCollections.UTILIZADORES)
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
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: return onError("Sessão expirada.")
        val email = user.email ?: return onError("Erro.")

        val cred = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(cred)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError("Erro ao atualizar password.") }
            }
            .addOnFailureListener { onError("Password atual incorreta.") }
    }

    fun toggleDia(dia: String) {
        val dias = _uiState.value.diasSelecionados.toMutableList()

        if (dia in dias) dias.remove(dia)
        else dias.add(dia)

        _uiState.value = _uiState.value.copy(diasSelecionados = dias)
    }

    fun updateHoraInicio(hora: String) {
        _uiState.value = _uiState.value.copy(horaInicio = hora)
    }

    fun updateHoraFim(hora: String) {
        _uiState.value = _uiState.value.copy(horaFim = hora)
    }

    fun updateObservacoes(txt: String) {
        _uiState.value = _uiState.value.copy(observacoes = txt)
    }

    // ---------- GUARDAR PREFERÊNCIAS EM SUBCOLEÇÃO ----------
    fun guardarPreferencias(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("Erro.")

        val data = mapOf(
            "id_beneficiario" to uid,
            "dias" to _uiState.value.diasSelecionados,
            "horaInicio" to _uiState.value.horaInicio,
            "horaFim" to _uiState.value.horaFim,
            "observacoes" to _uiState.value.observacoes,
            "timestamp" to System.currentTimeMillis()
        )

        // Subcoleção
        db.collection(FirestoreCollections.PREFERENCIAS_AGENDAMENTO)
            .document(uid)
            .collection("items")
            .add(data)
            .addOnSuccessListener {
                onSuccess()

                // LIMPAR CAMPOS APÓS GUARDAR
                _uiState.value = _uiState.value.copy(
                    diasSelecionados = emptyList(),
                    horaInicio = "",
                    horaFim = "",
                    observacoes = "",
                    success = "",
                    error = ""
                )
            }
            .addOnFailureListener {
                onError("Erro ao guardar preferências.")
            }
    }
}
