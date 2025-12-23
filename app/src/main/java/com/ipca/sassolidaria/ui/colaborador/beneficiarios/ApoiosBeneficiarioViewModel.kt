package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Apoio
import com.ipca.sassolidaria.data.repository.ApoiosRepository
import kotlinx.coroutines.launch

data class ApoiosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val apoios: List<Apoio> = emptyList()
)

class ApoiosBeneficiarioViewModel(
    private val repo: ApoiosRepository = ApoiosRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ApoiosUiState())
        private set

    fun carregar(idBeneficiario: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                uiState = uiState.copy(
                    isLoading = false,
                    apoios = repo.getApoiosDoBeneficiario(idBeneficiario)
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar apoios")
            }
        }
    }
}
