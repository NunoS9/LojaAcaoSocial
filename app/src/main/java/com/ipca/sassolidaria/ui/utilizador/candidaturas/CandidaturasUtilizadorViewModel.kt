package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.repository.CandidaturasRepository
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch

class CandidaturasUtilizadorViewModel(
    private val repo: CandidaturasRepository = CandidaturasRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var candidaturaState by mutableStateOf<ResultState<Candidatura?>>(ResultState.Idle)
        private set

    fun carregarCandidatura() {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            candidaturaState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        candidaturaState = ResultState.Loading

        viewModelScope.launch {
            try {
                val candidatura = repo.getCandidaturaByUserId(uid)
                candidaturaState = ResultState.Success(candidatura)
            } catch (e: Exception) {
                candidaturaState = ResultState.Error(
                    e.message ?: "Erro ao carregar candidatura."
                )
            }
        }
    }
}
