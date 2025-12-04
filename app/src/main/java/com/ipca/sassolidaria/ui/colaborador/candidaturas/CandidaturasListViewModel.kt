package com.ipca.sassolidaria.ui.colaborador.candidaturas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.repository.CandidaturasRepository
import kotlinx.coroutines.launch

class CandidaturasListViewModel(
    private val repo: CandidaturasRepository = CandidaturasRepository()
) : ViewModel() {

    var candidaturas by mutableStateOf<List<Candidatura>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        carregarCandidaturas()
    }

    fun carregarCandidaturas() {
        viewModelScope.launch {
            isLoading = true
            try {
                candidaturas = repo.getCandidaturasPendentes()
            } catch (e: Exception) {
                // Tratar erro se necessário
                candidaturas = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}