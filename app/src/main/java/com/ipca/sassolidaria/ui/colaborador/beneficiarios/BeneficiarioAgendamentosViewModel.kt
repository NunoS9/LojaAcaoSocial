package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import com.ipca.sassolidaria.data.repository.AgendamentosRepository

data class BeneficiarioAgendamentosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val agendamentos: List<Agendamento> = emptyList(),
    val filtroTipo: Tipo? = null,
    val filtroEstado: EstadoAgendamento? = null
)

class BeneficiarioAgendamentosViewModel(
    private val repo: AgendamentosRepository = AgendamentosRepository()
) : ViewModel() {

    var uiState by mutableStateOf(BeneficiarioAgendamentosUiState())
        private set

    private var listener: ListenerRegistration? = null

    fun start(idBeneficiario: String) {
        uiState = uiState.copy(isLoading = true, error = null)

        listener?.remove()
        listener = repo.observarAgendamentosDoBeneficiario(
            idBeneficiario = idBeneficiario,
            onChange = { lista ->
                uiState = uiState.copy(isLoading = false, agendamentos = lista, error = null)
            },
            onError = {
                uiState = uiState.copy(isLoading = false, error = "Erro ao carregar agendamentos")
            }
        )
    }

    fun alterarFiltroTipo(tipo: Tipo?) {
        uiState = uiState.copy(filtroTipo = tipo)
    }

    fun alterarFiltroEstado(estado: EstadoAgendamento?) {
        uiState = uiState.copy(filtroEstado = estado)
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
