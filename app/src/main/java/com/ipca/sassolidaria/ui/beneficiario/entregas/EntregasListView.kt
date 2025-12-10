package com.ipca.sassolidaria.ui.beneficiario.entregas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.ipca.sassolidaria.data.model.enums.EstadoEntrega
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState

private val GreenDark = Color(0xFF2E7D32)
private val ChipBlue = Color(0xFFE0E7FF)
private val ChipBlueText = Color(0xFF1E40AF)
private val ChipYellow = Color(0xFFFFF3CD)
private val ChipYellowText = Color(0xFF8D6E63)
private val ChipGreen = Color(0xFFE0F2F1)
private val ChipGreenText = Color(0xFF2E7D32)

@Composable
fun EntregasListView(
    navController: NavController,
    viewModel: EntregasListViewModel = viewModel()
) {
    val state = viewModel.entregasState

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
                Spacer(modifier = Modifier.width(48.dp)) // espaço para equilibrar
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
                                        // TODO: navegar para EntregaDetailView se quiseres
                                    }
                                )
                            }
                        }
                    }
                }

                ResultState.Idle -> {}
            }
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

            // Linha título + chip de estado
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

            // Itens só quando existir lista
            if (entrega.itens.isNotEmpty()) {
                entrega.itens.forEach { linha ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        // ícone simples tipo “•”
                        Text(
                            text = "•",
                            modifier = Modifier.padding(end = 6.dp),
                            color = Color.Gray
                        )
                        Text(
                            text = linha,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
        EstadoEntrega.PRONTA -> Triple("Pronta", ChipBlue, ChipBlueText)
        EstadoEntrega.PENDENTE -> Triple("Pendente", ChipYellow, ChipYellowText)
        EstadoEntrega.ENTREGUE -> Triple("Entregue", ChipGreen, ChipGreenText)
        else -> Triple(estado.name, Color.LightGray, Color.DarkGray)
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
