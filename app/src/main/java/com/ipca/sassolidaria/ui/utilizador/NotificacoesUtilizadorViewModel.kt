package com.ipca.sassolidaria.ui.utilizador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import kotlinx.coroutines.launch

data class NotificacoesUiState(
    val notificacoes: List<Notificacao> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificacoesUtilizadorViewModel(
    private val repo: NotificacoesRepository = NotificacoesRepository()
) : ViewModel() {

    var uiState by mutableStateOf(NotificacoesUiState())
        private set

    init {
        carregarNotificacoes()
    }

    fun carregarNotificacoes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val lista = repo.getNotificacoesDoUtilizador(uid)
                uiState = uiState.copy(
                    isLoading = false,
                    notificacoes = lista
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar notificações."
                )
            }
        }
    }
}
