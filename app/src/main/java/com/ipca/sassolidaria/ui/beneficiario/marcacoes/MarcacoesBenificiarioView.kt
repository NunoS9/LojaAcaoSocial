package com.ipca.sassolidaria.ui.beneficiario.marcacoes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.Agendamento
import com.ipca.sassolidaria.data.model.enums.EstadoAgendamento
import com.ipca.sassolidaria.data.model.enums.Tipo
import com.ipca.sassolidaria.ui.components.BottomBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarcacoesBeneficiarioView(
    navController: NavController,
    viewModel: MarcacoesBeneficiarioViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Marcações",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F7FB))
        ) {

            FiltrosMarcacoesRow(
                filtroAtual = state.filtro,
                onFiltroChange = { viewModel.alterarFiltro(it) }
            )

            Spacer(Modifier.height(8.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.errorMessage!!, color = Color.Red)
                    }
                }

                // dentro do else de when { ... } na view
                else -> {
                    val listaFiltrada = state.agendamentos.filter { ag ->
                        state.filtro == null || ag.estado == state.filtro
                    }

                    if (listaFiltrada.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sem marcações para este beneficiário", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(listaFiltrada) { agendamento ->
                                MarcacaoCard(
                                    agendamento = agendamento,
                                    onConfirmar = { viewModel.confirmarAgendamento(agendamento) },
                                    onRecusar = { viewModel.recusarAgendamento(agendamento) }
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun FiltrosMarcacoesRow(
    filtroAtual: EstadoAgendamento?,
    onFiltroChange: (EstadoAgendamento?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FilterChip(
            selected = filtroAtual == null,
            onClick = { onFiltroChange(null) },
            label = { Text("Todos") }
        )

        FilterChip(
            selected = filtroAtual == EstadoAgendamento.PENDENTE,
            onClick = { onFiltroChange(EstadoAgendamento.PENDENTE) },
            label = { Text("Pendentes") }
        )

        FilterChip(
            selected = filtroAtual == EstadoAgendamento.ACEITE,
            onClick = { onFiltroChange(EstadoAgendamento.ACEITE) },
            label = { Text("Aceites") }
        )

        FilterChip(
            selected = filtroAtual == EstadoAgendamento.RECUSADO,
            onClick = { onFiltroChange(EstadoAgendamento.RECUSADO) },
            label = { Text("Recusados") }
        )
    }
}

@Composable
private fun MarcacaoCard(
    agendamento: Agendamento,
    onConfirmar: () -> Unit,
    onRecusar: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dataTexto = agendamento.data_agendada?.let { sdf.format(it) } ?: "-"

    val (estadoTexto, estadoCor) = when (agendamento.estado) {
        EstadoAgendamento.ACEITE -> "Aceite"   to Color(0xFF2E7D32)
        EstadoAgendamento.RECUSADO -> "Recusado" to Color(0xFFD32F2F)
        EstadoAgendamento.PENDENTE -> "Pendente" to Color(0xFFF9A825)
    }

    val tipoTexto = when (agendamento.tipo) {
        Tipo.PEDIDO -> "Pedido"
        Tipo.APOIO  -> "Apoio"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Linha 1: ID + estado chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID do Agendamento: #${agendamento.id_agendamento}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(estadoCor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = estadoTexto,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Beneficiário",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Tipo: $tipoTexto",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6F6F6F)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Data do Agendamento: $dataTexto",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6F6F6F)
            )

            // Botões só se estiver pendente
            if (agendamento.estado == EstadoAgendamento.PENDENTE) {
                Spacer(Modifier.height(12.dp))

                Divider(color = Color(0xFFE0E0E0))

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onConfirmar) {
                        Text("Marcar")
                    }
                    TextButton(onClick = onRecusar) {
                        Text("Recusar")
                    }
                }
            }
        }
    }
}
