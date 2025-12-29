package com.ipca.sassolidaria.ui.colaborador.candidaturas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.CandidaturasRepository
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import kotlinx.coroutines.launch

class CandidaturaDetailViewModel(
    private val repo: CandidaturasRepository = CandidaturasRepository(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
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

    fun aprovarCandidatura(onSuccess: () -> Unit) {
        val itemAtual = candidatura ?: return

        viewModelScope.launch {
            try {
                // ✅ aprova + cria beneficiário + promove user
                val idBeneficiarioCriado = repo.aprovarCandidaturaEPromoverUser(itemAtual)

                // ✅ notificação para o BENEFICIÁRIO (destinatario = id_beneficiario)
                try {
                    notifRepo.notificarDestinatario(
                        destinatarioId = idBeneficiarioCriado,
                        tipo = TipoNotificacao.CANDIDATURA_ATUALIZADA,
                        titulo = "Candidatura aprovada",
                        conteudo = "A sua candidatura foi aprovada. A partir de agora já é beneficiário."
                    )
                } catch (_: Exception) {
                    // não rebenta o fluxo principal
                }

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun recusarCandidatura(onSuccess: () -> Unit) {
        val itemAtual = candidatura ?: return

        viewModelScope.launch {
            try {
                // ✅ recusa candidatura
                repo.recusarCandidatura(itemAtual.id_candidatura)

                // ✅ notificação para o UTILIZADOR (destinatario = id_utilizador que submeteu)
                try {
                    notifRepo.notificarDestinatario(
                        destinatarioId = itemAtual.id_utilizador,
                        tipo = TipoNotificacao.CANDIDATURA_ATUALIZADA,
                        titulo = "Candidatura rejeitada",
                        conteudo = "A sua candidatura foi rejeitada. Pode submeter novamente se necessário."
                    )
                } catch (_: Exception) {
                    // não rebenta o fluxo principal
                }

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
