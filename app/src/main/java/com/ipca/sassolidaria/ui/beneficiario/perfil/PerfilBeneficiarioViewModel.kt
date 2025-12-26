package com.ipca.sassolidaria.ui.beneficiario.perfil

import androidx.lifecycle.ViewModel
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
        loadUser()
        loadBeneficiarioId()
    }

    // CARREGAR DADOS DO UTILIZADOR (nome/email)
    private fun loadUser() {
        val uid = auth.currentUser?.uid ?: return

        db.collection(FirestoreCollections.UTILIZADORES)
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                _uiState.value = _uiState.value.copy(
                    nome = doc.getString("nome") ?: "",
                    email = doc.getString("email") ?: ""
                )
            }
    }

    // ✅ BUSCAR id_beneficiario real a partir da coleção BENEFICIARIOS
    private fun loadBeneficiarioId() {
        val uid = auth.currentUser?.uid ?: return

        db.collection(FirestoreCollections.BENEFICIARIOS)
            .whereEqualTo("id_utilizador", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                val idBenef = doc?.getString("id_beneficiario") ?: doc?.id ?: ""

                _uiState.value = _uiState.value.copy(id_beneficiario = idBenef)
            }
            .addOnFailureListener {
                // não travamos a UI; só deixamos vazio
                _uiState.value = _uiState.value.copy(id_beneficiario = "")
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

    // ✅ GUARDAR PREFERÊNCIAS COM id_beneficiario (não UID)
    fun guardarPreferencias(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val idBeneficiario = _uiState.value.id_beneficiario

        if (idBeneficiario.isBlank()) {
            onError("Não foi possível identificar o beneficiário.")
            return
        }

        val data = mapOf(
            "id_beneficiario" to idBeneficiario,
            "dias" to _uiState.value.diasSelecionados,
            "horaInicio" to _uiState.value.horaInicio,
            "horaFim" to _uiState.value.horaFim,
            "observacoes" to _uiState.value.observacoes,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection(FirestoreCollections.PREFERENCIAS_AGENDAMENTO)
            .document(idBeneficiario) // ✅ agora é o id_beneficiario
            .collection("items")
            .add(data)
            .addOnSuccessListener {
                onSuccess()

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
