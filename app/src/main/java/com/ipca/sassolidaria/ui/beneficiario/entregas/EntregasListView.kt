package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState

private val GreenDark = Color(0xFF2E7D32)
private val ChipBlue = Color(0xFFE0E7FF)
private val ChipBlueText = Color(0xFF1E40AF)
private val ChipYellow = Color(0xFFFFF3CD)
private val ChipYellowText = Color(0xFF8D6E63)
private val ChipRed = Color(0xFFFFCDD2)
private val ChipRedText = Color(0xFFC62828)
private val ChipGreen = Color(0xFFE0F2F1)
private val ChipGreenText = Color(0xFF2E7D32)

@Composable
fun EntregasListView(
    navController: NavController,
    viewModel: EntregasListViewModel = viewModel()
) {
    val state = viewModel.entregasState
    val detalhesState = viewModel.detalhesState

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
                Text(
                    text = "Minhas Entregas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (state) {
                is ResultState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ResultState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is ResultState.Success -> {
                    val entregas = state.data

                    if (entregas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ainda não existem entregas.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(entregas) { entrega ->
                                EntregaCard(
                                    entrega = entrega,
                                    onVerDetalhes = {
                                        showDialog = true
                                        viewModel.carregarItensEntrega(entrega.id)
                                    }
                                )
                            }
                        }
                    }
                }

                ResultState.Idle -> {}
            }
        }

        // --------- POPUP DE DETALHES ----------
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    viewModel.limparDetalhes()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            viewModel.limparDetalhes()
                        }
                    ) {
                        Text("Fechar")
                    }
                },
                title = {
                    Text(text = "Itens da entrega")
                },
                text = {
                    when (detalhesState) {
                        is ResultState.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is ResultState.Error -> {
                            Text(
                                text = (detalhesState as ResultState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        is ResultState.Success -> {
                            val itens = (detalhesState as ResultState.Success<List<String>>).data
                            if (itens.isEmpty()) {
                                Text("Nenhum item associado a esta entrega.")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    itens.forEach { linha ->
                                        Text("• $linha")
                                    }
                                }
                            }
                        }

                        ResultState.Idle -> {
                            Text("Carregue em 'Ver Detalhes' para ver os itens.")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun EntregaCard(
    entrega: EntregaUi,
    onVerDetalhes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entrega #${entrega.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                EstadoEntregaChip(estado = entrega.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (entrega.dataHora.isNotBlank()) {
                Text(
                    text = entrega.dataHora,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Divider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onVerDetalhes,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenDark,
                        contentColor = Color.White
                    )
                ) {
                    Text("Ver Detalhes")
                }
            }
        }
    }
}

@Composable
private fun EstadoEntregaChip(estado: EstadoEntrega) {
    val (texto, bg, fg) = when (estado) {
        EstadoEntrega.PENDENTE ->
            Triple("Pendente", ChipYellow, ChipYellowText)

        EstadoEntrega.PRONTA ->
            Triple("Pronta", ChipBlue, ChipBlueText)

        EstadoEntrega.ENTREGUE ->
            Triple("Entregue", ChipGreen, ChipGreenText)

        EstadoEntrega.CANCELADA ->
            Triple("Cancelada", ChipRed, ChipRedText)

        else ->
            Triple(estado.name, Color.LightGray, Color.DarkGray)
    }

    Box(
        modifier = Modifier
            .background(color = bg, shape = MaterialTheme.shapes.large)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
