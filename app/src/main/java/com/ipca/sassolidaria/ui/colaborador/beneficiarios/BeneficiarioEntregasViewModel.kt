package com.ipca.sassolidaria.ui.colaborador.beneficiarios

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.Entrega
import com.ipca.sassolidaria.data.model.ItemEntrega
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.data.repository.NotificacoesRepository
import com.ipca.sassolidaria.utils.FirestoreCollections
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class EntregaUi(
    val entrega: Entrega,
    val dataAgendada: Date? = null,
    val itensResumo: String = ""
)

data class BeneficiarioEntregasUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val entregas: List<EntregaUi> = emptyList()
)

class BeneficiarioEntregasViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val notifRepo: NotificacoesRepository = NotificacoesRepository()
) : ViewModel() {

    private val _uiState = mutableStateOf(BeneficiarioEntregasUiState())
    val uiState: State<BeneficiarioEntregasUiState> = _uiState

    private var listener: ListenerRegistration? = null

    private val cacheAgendamentoData = mutableMapOf<String, Date?>()
    private val cacheProdutoNome = mutableMapOf<String, String>()

    fun start(idBeneficiario: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        listener?.remove()
        listener = db.collection(FirestoreCollections.ENTREGAS)
            .whereEqualTo("id_beneficiario", idBeneficiario)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao carregar entregas")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, entregas = emptyList())
                    return@addSnapshotListener
                }

                val entregasBase = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Entrega::class.java)?.copy(id_entrega = doc.id)
                }

                // 1) mostra imediatamente
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    entregas = entregasBase.map { EntregaUi(entrega = it) },
                    error = null
                )

                // 2) enriquecer em paralelo
                viewModelScope.launch {
                    try {
                        val enriched = enriquecerEntregas(entregasBase)
                            .sortedByDescending { it.dataAgendada ?: Date(0) }

                        _uiState.value = _uiState.value.copy(entregas = enriched, error = null)
                    } catch (ex: Exception) {
                        _uiState.value = _uiState.value.copy(
                            error = ex.message ?: "Erro ao carregar detalhes das entregas"
                        )
                    }
                }
            }
    }

    private suspend fun enriquecerEntregas(entregas: List<Entrega>): List<EntregaUi> {
        if (entregas.isEmpty()) return emptyList()

        // 1) Agendamentos
        val idsAg = entregas.map { it.id_agendamento }.distinct().filter { it.isNotBlank() }
        val faltamAg = idsAg.filter { !cacheAgendamentoData.containsKey(it) }

        if (faltamAg.isNotEmpty()) {
            val jobs = faltamAg.map { idAg ->
                viewModelScope.async {
                    val snap = db.collection(FirestoreCollections.AGENDAMENTOS)
                        .document(idAg)
                        .get()
                        .await()

                    val ag = snap.toObject(Agendamento::class.java)
                    cacheAgendamentoData[idAg] = ag?.data_agendada
                }
            }
            jobs.awaitAll()
        }

        // 2) Itens entrega
        val itensPorEntrega: Map<String, List<ItemEntrega>> = run {
            val jobs = entregas.map { ent ->
                viewModelScope.async {
                    val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                        .whereEqualTo("id_entrega", ent.id_entrega)
                        .get()
                        .await()

                    ent.id_entrega to itensSnap.documents.mapNotNull { d ->
                        d.toObject(ItemEntrega::class.java)
                    }
                }
            }
            jobs.awaitAll().toMap()
        }

        // 3) Produtos
        val idsProdutos = itensPorEntrega.values.flatten()
            .map { it.id_produto }
            .distinct()
            .filter { it.isNotBlank() }

        val faltamProd = idsProdutos.filter { !cacheProdutoNome.containsKey(it) }

        if (faltamProd.isNotEmpty()) {
            val jobs = faltamProd.map { idProd ->
                viewModelScope.async {
                    val pSnap = db.collection(FirestoreCollections.PRODUTOS)
                        .document(idProd)
                        .get()
                        .await()

                    val prod = pSnap.toObject(Produto::class.java)?.copy(id_produto = pSnap.id)
                    if (prod != null) cacheProdutoNome[idProd] = prod.nome
                }
            }
            jobs.awaitAll()
        }

        // 4) UI
        return entregas.map { ent ->
            val dataAg = cacheAgendamentoData[ent.id_agendamento]

            val itens = itensPorEntrega[ent.id_entrega].orEmpty()
            val resumo = if (itens.isEmpty()) "" else {
                val porProduto = itens.groupBy { it.id_produto }
                    .mapValues { (_, list) -> list.sumOf { it.quantidade } }

                porProduto.entries.joinToString(", ") { (idProd, qtd) ->
                    val nome = cacheProdutoNome[idProd] ?: idProd
                    "$nome x$qtd"
                }
            }

            EntregaUi(
                entrega = ent,
                dataAgendada = dataAg,
                itensResumo = resumo
            )
        }
    }

    fun confirmarEntregaEAtualizarStock(idEntrega: String) {
        viewModelScope.launch {
            try {
                val uidColab = auth.currentUser?.uid ?: run {
                    _uiState.value = _uiState.value.copy(error = "Colaborador não autenticado.")
                    return@launch
                }

                val itensSnap = db.collection(FirestoreCollections.ITENS_ENTREGA)
                    .whereEqualTo("id_entrega", idEntrega)
                    .get()
                    .await()

                val itens = itensSnap.documents.mapNotNull { it.toObject(ItemEntrega::class.java) }
                if (itens.isEmpty()) {
                    _uiState.value = _uiState.value.copy(error = "Esta entrega não tem itens.")
                    return@launch
                }

                val entregaRef = db.collection(FirestoreCollections.ENTREGAS).document(idEntrega)

                db.runTransaction { tx ->
                    val entregaSnap = tx.get(entregaRef)
                    val estadoAtual = entregaSnap.getString("estado") ?: EstadoEntrega.PENDENTE.name

                    if (estadoAtual != EstadoEntrega.PRONTA.name) {
                        throw IllegalStateException("Só podes confirmar entregas no estado PRONTA.")
                    }

                    val porProduto = itens.groupBy { it.id_produto }
                        .mapValues { (_, list) -> list.sumOf { it.quantidade } }

                    for ((idProd, qtd) in porProduto) {
                        val prodRef = db.collection(FirestoreCollections.PRODUTOS).document(idProd)
                        val prodSnap = tx.get(prodRef)

                        val disponivel = (prodSnap.getLong("quantidade_disponivel") ?: 0L).toInt()
                        if (qtd > disponivel) throw IllegalStateException("Stock insuficiente para o produto ($idProd).")

                        tx.update(prodRef, "quantidade_disponivel", disponivel - qtd)
                    }

                    tx.update(
                        entregaRef,
                        mapOf(
                            "estado" to EstadoEntrega.ENTREGUE.name,
                            "id_colaborador" to uidColab
                        )
                    )
                    null
                }.await()

                try {
                    val entregaSnap = entregaRef.get().await()
                    val idBeneficiario = entregaSnap.getString("id_beneficiario") ?: ""

                    if (idBeneficiario.isNotBlank()) {
                        notifRepo.notificarDestinatario(
                            destinatarioId = idBeneficiario,
                            tipo = TipoNotificacao.ENTREGA_ATUALIZADA,
                            titulo = "Entrega entregue",
                            conteudo = "A sua entrega #$idEntrega foi marcada como entregue."
                        )
                    }
                } catch (_: Exception) {
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Erro ao confirmar entrega")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
