package com.ipca.sassolidaria.ui.colaborador.candidaturas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.repository.CandidaturasRepository
import kotlinx.coroutines.launch

class CandidaturaDetailViewModel(
    private val repo: CandidaturasRepository = CandidaturasRepository()
) : ViewModel() {

    var candidatura by mutableStateOf<Candidatura?>(null)
        private set

    var isLoading by mutableStateOf(false)

    fun carregarCandidatura(id: String) {
        viewModelScope.launch {
            isLoading = true
            candidatura = repo.getCandidaturaById(id)
            isLoading = false
        }
    }

    // ATUALIZADO: Agora chama a função que cria o beneficiário e atualiza o user
    fun aprovarCandidatura(onSuccess: () -> Unit) {
        val itemAtual = candidatura ?: return

        viewModelScope.launch {
            try {
                repo.aprovarCandidaturaEPromoverUser(itemAtual)
                onSuccess()
            } catch (e: Exception) {
                // Aqui podes tratar erros (ex: mostrar Toast)
                e.printStackTrace()
            }
        }
    }

    // Mantém-se igual, apenas muda o estado para REJEITADA
    fun recusarCandidatura(onSuccess: () -> Unit) {
        val id = candidatura?.id_candidatura ?: return
        viewModelScope.launch {
            try {
                repo.recusarCandidatura(id)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}