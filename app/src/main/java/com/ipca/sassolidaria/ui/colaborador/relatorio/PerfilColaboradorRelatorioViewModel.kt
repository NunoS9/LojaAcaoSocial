package com.ipca.sassolidaria.ui.colaborador.relatorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ipca.sassolidaria.utils.FirestoreCollections
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilColaboradorRelatorioViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    data class Contagens(
        val pedidosPendentes: Int = 0,
        val pedidosEntregues: Int = 0,
        val pedidosCancelados: Int = 0,

        val entregasPendentes: Int = 0,
        val entregasProntas: Int = 0,
        val entregasEntregues: Int = 0,
        val entregasCanceladas: Int = 0,

        val candidaturasEmAnalise: Int = 0,
        val candidaturasAprovadas: Int = 0,
        val candidaturasRejeitadas: Int = 0
    )

    data class RelatorioData(
        val contagens: Contagens,
        val pedidosRows: List<List<String>>,
        val entregasRows: List<List<String>>,
        val stockRows: List<List<String>>
    )

    private val cacheNomeBeneficiario = mutableMapOf<String, String>()
    private val cacheNomeUser = mutableMapOf<String, String>()
    private val cacheAgendamentoDate = mutableMapOf<String, Date?>()

    private val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun carregarRelatorioCompleto(
        start: Date?,
        end: Date?,
        onResult: (ResultState<RelatorioData>) -> Unit
    ) {
        onResult(ResultState.Loading)

        viewModelScope.launch {
            try {
                // -------------------------
                // 1) Buscar docs base
                // -------------------------
                val pedidosDocs = fetchDocsByDate(
                    collection = FirestoreCollections.PEDIDOS,
                    dateField = "data_pedido",
                    start = start,
                    end = end
                )

                val candidaturasDocs = fetchDocsByDate(
                    collection = FirestoreCollections.CANDIDATURAS,
                    dateField = "data_submissao",
                    start = start,
                    end = end
                )

                // Entregas: não têm data -> buscar todas (ou um limite alto)
                val entregasDocs = db.collection(FirestoreCollections.ENTREGAS)
                    .get()
                    .await()
                    .documents

                // -------------------------
                // 2) Linhas (TUDO)
                // -------------------------
                val pedidosRows = buildPedidosRowsFromDocs(pedidosDocs)

                // Entregas: aplicar filtro do mês usando data_agendada do agendamento
                val entregasRows = buildEntregasRowsFromDocs(entregasDocs, start, end)

                val stockRows = fetchStockRows()

                // -------------------------
                // 3) Contagens (locais)
                // -------------------------
                val counts = Contagens(
                    pedidosPendentes = countEstado(pedidosDocs, "PENDENTE"),
                    pedidosEntregues = countEstado(pedidosDocs, "ENTREGUE"),
                    pedidosCancelados = countEstado(pedidosDocs, "CANCELADO"),

                    // Entregas: contar com base nas linhas filtradas (já respeitam mês/tudo)
                    entregasPendentes = entregasRows.count { it.getOrNull(1) == "PENDENTE" },
                    entregasProntas = entregasRows.count { it.getOrNull(1) == "PRONTA" },
                    entregasEntregues = entregasRows.count { it.getOrNull(1) == "ENTREGUE" },
                    entregasCanceladas = entregasRows.count { it.getOrNull(1) == "CANCELADA" },

                    candidaturasEmAnalise = countEstado(candidaturasDocs, "EM_ANALISE"),
                    candidaturasAprovadas = countEstado(candidaturasDocs, "APROVADA"),
                    candidaturasRejeitadas = countEstado(candidaturasDocs, "REJEITADA")
                )

                onResult(
                    ResultState.Success(
                        RelatorioData(
                            contagens = counts,
                            pedidosRows = pedidosRows,
                            entregasRows = entregasRows,
                            stockRows = stockRows
                        )
                    )
                )
            } catch (e: Exception) {
                onResult(ResultState.Error(e.message ?: "Erro ao gerar relatório."))
            }
        }
    }

    // --------------------------------------------------------------------
    // Fetch genérico: só range por data (sem whereEqualTo + range)
    // --------------------------------------------------------------------
    private suspend fun fetchDocsByDate(
        collection: String,
        dateField: String,
        start: Date?,
        end: Date?
    ): List<DocumentSnapshot> {
        var q: Query = db.collection(collection)

        if (start != null && end != null) {
            q = q.whereGreaterThanOrEqualTo(dateField, start)
                .whereLessThan(dateField, end)
        }

        return q.get().await().documents
    }

    // --------------------------------------------------------------------
    // Count local
    // --------------------------------------------------------------------
    private fun countEstado(docs: List<DocumentSnapshot>, estado: String): Int {
        return docs.count { (it.getString("estado") ?: "") == estado }
    }

    // --------------------------------------------------------------------
    // Pedidos rows: TUDO do mês / TUDO sem filtro
    // --------------------------------------------------------------------
    private suspend fun buildPedidosRowsFromDocs(
        docs: List<DocumentSnapshot>
    ): List<List<String>> {

        val ordered = docs.sortedByDescending { it.getTimestamp("data_pedido")?.toDate() }

        return ordered.map { d ->
            val estado = d.getString("estado") ?: "—"

            // pedidos.id_beneficiario = ID DO UTILIZADOR (como tens no Firestore)
            val userId = d.getString("id_beneficiario") ?: ""
            val nome = resolveNomeFromUserOrBeneficiario(userId)

            val data = d.getTimestamp("data_pedido")?.toDate()
            val dataStr = data?.let { df.format(it) } ?: "—"

            // ✅ sem descrição (como pediste)
            listOf(nome, estado, dataStr)
        }
    }

    // --------------------------------------------------------------------
    // Entregas rows: TUDO do mês / TUDO sem filtro, baseado no agendamento.data_agendada
    // --------------------------------------------------------------------
    private suspend fun buildEntregasRowsFromDocs(
        docs: List<DocumentSnapshot>,
        start: Date?,
        end: Date?
    ): List<List<String>> {

        // 1) Enriquecer com data_agendada (para filtrar e ordenar)
        val enriched = docs.map { d ->
            val estado = d.getString("estado") ?: "—"
            val tipo = d.getString("tipo") ?: "—"

            val benId = d.getString("id_beneficiario") ?: ""
            val nomeBen = resolveNomeBeneficiario(benId)

            val agId = d.getString("id_agendamento") ?: ""
            val dataAgendada = resolveDataAgendadaDate(agId)
            val agendamentoStr = dataAgendada?.let { df.format(it) } ?: "—"

            Triple(dataAgendada, estado, listOf(nomeBen, estado, tipo, agendamentoStr))
        }

        // 2) Filtrar por mês se tiver start/end
        val filtered = if (start != null && end != null) {
            enriched.filter { (dt, _, _) ->
                dt != null && !dt.before(start) && dt.before(end)
            }
        } else {
            enriched
        }

        // 3) Ordenar por data_agendada desc (mais recentes primeiro)
        return filtered
            .sortedByDescending { it.first }
            .map { it.third }
    }

    // devolve Date? (para filtrar/ordenar)
    private suspend fun resolveDataAgendadaDate(idAgendamento: String): Date? {
        if (idAgendamento.isBlank()) return null
        if (cacheAgendamentoDate.containsKey(idAgendamento)) return cacheAgendamentoDate[idAgendamento]

        val doc = db.collection(FirestoreCollections.AGENDAMENTOS)
            .document(idAgendamento)
            .get()
            .await()

        val dt = doc.getTimestamp("data_agendada")?.toDate()
        cacheAgendamentoDate[idAgendamento] = dt
        return dt
    }

    // --------------------------------------------------------------------
    // Stock rows
    // --------------------------------------------------------------------
    private suspend fun fetchStockRows(): List<List<String>> {
        val snap = db.collection(FirestoreCollections.PRODUTOS)
            .orderBy("nome", Query.Direction.ASCENDING)
            .get()
            .await()

        return snap.documents.map { d ->
            val nome = d.getString("nome") ?: "—"
            val disp = (d.getLong("quantidade_disponivel") ?: 0L).toString()
            val total = (d.getLong("quantidade") ?: 0L).toString()
            listOf(nome, disp, total)
        }
    }

    // --------------------------------------------------------------------
    // Nome resolvers
    // --------------------------------------------------------------------
    private suspend fun resolveNomeFromUserOrBeneficiario(id: String): String {
        if (id.isBlank()) return "—"
        cacheNomeUser[id]?.let { return it }

        // 1) users/{id}
        val userDoc = db.collection("users").document(id).get().await()
        val nomeUser = userDoc.getString("nome")
        if (!nomeUser.isNullOrBlank()) {
            cacheNomeUser[id] = nomeUser
            return nomeUser
        }

        // 2) beneficiarios/{id}
        val benDoc = db.collection(FirestoreCollections.BENEFICIARIOS).document(id).get().await()
        val nomeBen = benDoc.getString("nome")
        if (!nomeBen.isNullOrBlank()) {
            cacheNomeUser[id] = nomeBen
            return nomeBen
        }

        // 3) beneficiarios where id_utilizador == id (1 campo -> normalmente não pede índice)
        val queryBen = db.collection(FirestoreCollections.BENEFICIARIOS)
            .whereEqualTo("id_utilizador", id)
            .limit(1)
            .get()
            .await()

        val nomeFound = queryBen.documents.firstOrNull()?.getString("nome")
        if (!nomeFound.isNullOrBlank()) {
            cacheNomeUser[id] = nomeFound
            return nomeFound
        }

        return id.take(8)
    }

    private suspend fun resolveNomeBeneficiario(idBeneficiarioDoc: String): String {
        if (idBeneficiarioDoc.isBlank()) return "—"
        cacheNomeBeneficiario[idBeneficiarioDoc]?.let { return it }

        val doc = db.collection(FirestoreCollections.BENEFICIARIOS)
            .document(idBeneficiarioDoc)
            .get()
            .await()

        val nome = doc.getString("nome") ?: idBeneficiarioDoc.take(8)
        cacheNomeBeneficiario[idBeneficiarioDoc] = nome
        return nome
    }
}
