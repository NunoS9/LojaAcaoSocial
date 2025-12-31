package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.model.DocumentoCandidatura
import com.ipca.sassolidaria.data.model.enums.EstadoCandidatura
import com.ipca.sassolidaria.data.model.enums.GrauCurso
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.CandidaturasRepository
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.launch
import java.util.Date

class CandidaturaCreateViewModel(
    private val repo: CandidaturasRepository = CandidaturasRepository(),
    private val notificacoesRepo: NotificacoesRepository = NotificacoesRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var nome by mutableStateOf("")
    var cc by mutableStateOf("")
    var email by mutableStateOf("")
    var telemovel by mutableStateOf("")
    var anoLetivo by mutableStateOf("")
    var curso by mutableStateOf("")
    var numEstudante by mutableStateOf("")
    var grauCurso by mutableStateOf(GrauCurso.CTESP)

    var apoioAlimentacao by mutableStateOf(false)
    var apoioLimpeza by mutableStateOf(false)
    var apoioHigiene by mutableStateOf(false)
    var faes by mutableStateOf(false)
    var bolsa by mutableStateOf(false)
    var obsBolsa by mutableStateOf("")


    val documentos = mutableStateListOf<DocumentoCandidatura>()

    var submitState by mutableStateOf<ResultState<Unit>>(ResultState.Idle)
        private set

    fun adicionarDocumento(doc: DocumentoCandidatura) {
        documentos.add(doc)
        limparErros()
    }

    fun removerDocumento(idDocumentoTemp: String) {
        documentos.removeAll { it.id_documento == idDocumentoTemp }
    }

    fun limparDocs() {
        documentos.clear()
    }

    fun submeterCandidatura() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            submitState = ResultState.Error("Utilizador não autenticado.")
            return
        }

        if (nome.isBlank() || email.isBlank() || curso.isBlank()) {
            submitState = ResultState.Error("Preencha os campos obrigatórios.")
            return
        }


        if (!faes && documentos.isEmpty()) {
            submitState = ResultState.Error("Tem de anexar pelo menos um documento (ou assinalar FAES).")
            return
        }

        submitState = ResultState.Loading

        viewModelScope.launch {
            try {
                val candidatura = Candidatura(
                    id_candidatura = "",
                    id_utilizador = uid,
                    nome = nome,
                    data_nascimento = null,
                    cc = cc,
                    email = email,
                    telemovel = telemovel.toIntOrNull() ?: 0,
                    ano_letivo = anoLetivo,
                    grau_curso = grauCurso,
                    curso = curso,
                    num_estudante = numEstudante.toIntOrNull() ?: 0,
                    apoio_alimentacao = apoioAlimentacao,
                    apoio_limpeza = apoioLimpeza,
                    apoio_higiene = apoioHigiene,
                    faes = faes,
                    bolsa = bolsa,
                    obs_bolsa = obsBolsa,
                    assinatura = "",
                    data_submissao = Date(),
                    estado = EstadoCandidatura.EM_ANALISE
                )

                // 1) cria candidatura e obtém id
                val idCandidatura = repo.createCandidatura(candidatura)

                // 2) guarda documentos (se existirem)
                if (documentos.isNotEmpty()) {
                    // garante data_upload
                    val docsComData = documentos.map {
                        it.copy(
                            data_upload = it.data_upload ?: Date()
                        )
                    }
                    repo.guardarDocumentosDaCandidatura(idCandidatura, docsComData)
                }

                // 3) notifica colaboradores
                runCatching {
                    notificacoesRepo.notificarTodosColaboradores(
                        tipo = TipoNotificacao.NOVA_CANDIDATURA,
                        titulo = "Nova candidatura",
                        conteudo = "Foi submetida uma nova candidatura por: $nome"
                    )
                }

                submitState = ResultState.Success(Unit)
            } catch (e: Exception) {
                submitState = ResultState.Error(e.message ?: "Erro ao submeter a candidatura.")
            }
        }
    }

    fun limparErros() {
        if (submitState is ResultState.Error) {
            submitState = ResultState.Idle
        }
    }
}
