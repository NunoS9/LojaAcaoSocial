package com.ipca.sassolidaria.ui.colaborador.candidaturas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.model.DocumentoCandidatura
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


    var documentos by mutableStateOf<List<DocumentoCandidatura>>(emptyList())
        private set

    fun carregarCandidatura(id: String) {
        viewModelScope.launch {
            isLoading = true
            candidatura = repo.getCandidaturaById(id)


            documentos = repo.getDocumentosDaCandidatura(id)

            isLoading = false
        }
    }

    fun aprovarCandidatura(onSuccess: () -> Unit) {
        val itemAtual = candidatura ?: return

        viewModelScope.launch {
            try {
                val idBeneficiarioCriado = repo.aprovarCandidaturaEPromoverUser(itemAtual)

                try {
                    notifRepo.notificarDestinatario(
                        destinatarioId = idBeneficiarioCriado,
                        tipo = TipoNotificacao.CANDIDATURA_ATUALIZADA,
                        titulo = "Candidatura aprovada",
                        conteudo = "A sua candidatura foi aprovada. A partir de agora já é beneficiário."
                    )
                } catch (_: Exception) {
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
                repo.recusarCandidatura(itemAtual.id_candidatura)

                try {
                    notifRepo.notificarDestinatario(
                        destinatarioId = itemAtual.id_utilizador,
                        tipo = TipoNotificacao.CANDIDATURA_ATUALIZADA,
                        titulo = "Candidatura rejeitada",
                        conteudo = "A sua candidatura foi rejeitada. Pode submeter novamente se necessário."
                    )
                } catch (_: Exception) {
                }

                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
