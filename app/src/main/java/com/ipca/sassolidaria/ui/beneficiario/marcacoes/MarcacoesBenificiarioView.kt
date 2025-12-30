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
import java.util.Locale

private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)

private val GreenDark = Color(0xFF004B3C)

// chips (soft)
private val ChipGreenBg = Color(0xFFE8F5E9)
private val ChipGreenText = Color(0xFF2E7D32)

private val ChipYellow = Color(0xFFFFF3CD)
private val ChipYellowText = Color(0xFF8D6E63)
private val ChipRedBg = Color(0xFFFFCDD2)
private val ChipRedText = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarcacoesBeneficiarioView(
    navController: NavController,
    viewModel: MarcacoesBeneficiarioViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Marcações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScreenBg)
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
                    ) { CircularProgressIndicator() }
                }

                state.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }

                else -> {
                    val listaFiltrada = state.agendamentos.filter { ag ->
                        state.filtro == null || ag.estado == state.filtro
                    }

                    if (listaFiltrada.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { Text("Sem marcações para este beneficiário", color = Color.Gray) }
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
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dataTexto = agendamento.data_agendada?.let { sdf.format(it) } ?: "—"

    // chip (igual ideia do teu Triple)
    val (estadoTexto, estadoBg, estadoFg) = when (agendamento.estado) {
        EstadoAgendamento.ACEITE ->
            Triple("Aceite", ChipGreenBg, ChipGreenText)

        EstadoAgendamento.PENDENTE ->
            Triple("Pendente", ChipYellow, ChipYellowText)

        EstadoAgendamento.RECUSADO ->
            Triple("Recusado", ChipRedBg, ChipRedText)

        else ->
            Triple(agendamento.estado.name, Color.LightGray, Color.DarkGray)
    }

    val tipoTexto = when (agendamento.tipo) {
        Tipo.PEDIDO -> "Pedido"
        Tipo.APOIO -> "Apoio"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top row: "Marcação #ID" + chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Marcação #${agendamento.id_agendamento}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(estadoBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = estadoTexto,
                        color = estadoFg,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = Color(0xFFEFEFEF))

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Detalhes",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Tipo: $tipoTexto",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6F6F6F)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Data do agendamento: $dataTexto",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6F6F6F)
            )

            // Botões só se pendente (mais bonitos)
            if (agendamento.estado == EstadoAgendamento.PENDENTE) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFEFEFEF))
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = GreenDark
                        )
                    ) {
                        Text("Marcar")
                    }

                    Button(
                        onClick = onRecusar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = ChipRedText
                        )
                    ) {
                        Text("Recusar")
                    }
                }
            }
        }
    }
}
