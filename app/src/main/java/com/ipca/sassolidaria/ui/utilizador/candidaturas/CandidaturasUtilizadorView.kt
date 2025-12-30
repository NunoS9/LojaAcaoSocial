package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.model.enums.EstadoCandidatura
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState
import java.text.SimpleDateFormat
import java.util.Locale

private val GreenDark = Color(0xFF004B3C)
private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturasUtilizadorView(
    navController: NavController,
    viewModel: CandidaturasUtilizadorViewModel = viewModel()
) {
    val state = viewModel.candidaturaState

    LaunchedEffect(Unit) {
        viewModel.carregarCandidaturas()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Minhas Candidaturas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg,
                        scrolledContainerColor = ScreenBg
                    )
                )
                Divider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CANDIDATURA_CREATE) },
                containerColor = GreenDark,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Nova candidatura")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = ScreenBg
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (state) {
                is ResultState.Idle, is ResultState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ResultState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarCandidaturas() }) {
                            Text("Tentar novamente")
                        }
                    }
                }

                is ResultState.Success -> {
                    val candidaturas = state.data

                    if (candidaturas.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Ainda não submeteu nenhuma candidatura.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(candidaturas) { c ->
                                CandidaturaDetalheCard(candidatura = c)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidaturaDetalheCard(
    candidatura: Candidatura,
    modifier: Modifier = Modifier
) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dataSubmissao = candidatura.data_submissao?.let { formatter.format(it) } ?: "-"

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = candidatura.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Curso: ${candidatura.curso}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Nº Estudante: ${candidatura.num_estudante}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(10.dp))

            EstadoCandidaturaBadge(estado = candidatura.estado)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Data de submissão: $dataSubmissao",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun EstadoCandidaturaBadge(estado: EstadoCandidatura) {
    // ✅ Aqui é onde tu decides o “texto”
    // Se queres que o utilizador veja “Pendente”, mete “Pendente” em EM_ANALISE.
    val (label, bg, fg, border) = when (estado) {
        EstadoCandidatura.EM_ANALISE -> Quad(
            "Pendente",               // ou "Em análise" se preferires
            Color(0xFFFFF8E1),
            Color(0xFFF9A825),
            Color(0xFFE6D59E)
        )

        EstadoCandidatura.APROVADA -> Quad(
            "Aprovada",
            Color(0xFFD4EDDA),
            Color(0xFF2E7D32),
            Color(0xFFAED7B7)
        )

        EstadoCandidatura.REJEITADA -> Quad(
            "Rejeitada",
            Color(0xFFF8D7DA),
            Color(0xFFD32F2F),
            Color(0xFFE6B3B8)
        )
    }

    AssistChip(
        onClick = { },
        label = {
            Text(
                text = label,
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg, labelColor = fg),
        border = BorderStroke(1.dp, border)
    )
}

private data class Quad(val a: String, val b: Color, val c: Color, val d: Color)
