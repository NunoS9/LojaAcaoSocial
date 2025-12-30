package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState

class CandidaturasUtilizadorViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var candidaturaState by mutableStateOf<ResultState<List<Candidatura>>>(ResultState.Idle)
        private set

    private var listener: ListenerRegistration? = null

    fun carregarCandidaturas() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            candidaturaState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        candidaturaState = ResultState.Loading
        listener?.remove()

        // ⚠️ CONFIRMA o nome do campo no Firestore.
        // Normalmente é "id_utilizador". Se no teu for outro, troca aqui.
        listener = db.collection(FirestoreCollections.CANDIDATURAS)
            .whereEqualTo("id_utilizador", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    candidaturaState = ResultState.Error("Erro ao ouvir candidaturas: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    candidaturaState = ResultState.Error("Erro ao carregar candidaturas.")
                    return@addSnapshotListener
                }

                val candidaturas = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Candidatura::class.java)
                    } catch (_: Exception) {
                        null
                    }
                }.sortedByDescending { it.data_submissao }

                candidaturaState = ResultState.Success(candidaturas)
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
