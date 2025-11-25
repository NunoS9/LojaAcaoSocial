package com.ipca.sassolidaria.ui.utilizador.candidaturas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.data.model.Candidatura
import com.ipca.sassolidaria.data.model.enums.EstadoCandidatura
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.ipca.sassolidaria.core.Routes
import androidx.compose.ui.graphics.Color

private val GreenDark = Color(0xFF004B3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidaturasUtilizadorView(
    navController: NavController,
    viewModel: CandidaturasUtilizadorViewModel = viewModel()
) {
    val state = viewModel.candidaturaState

    LaunchedEffect(Unit) {
        viewModel.carregarCandidatura()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Candidaturas") }
            )
        },
        bottomBar = { BottomBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CANDIDATURA_CREATE) },
                containerColor = GreenDark,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Nova candidatura"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (state) {
                is ResultState.Idle,
                is ResultState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                        Button(onClick = { viewModel.carregarCandidatura() }) {
                            Text("Tentar novamente")
                        }
                    }
                }

                is ResultState.Success -> {
                    val candidatura = state.data
                    if (candidatura == null) {
                        SemCandidaturaView(
                            onCriarCandidatura = {
                                // TODO: navegar para ecrã de criação de candidatura
                            },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        CandidaturaDetalheCard(
                            candidatura = candidatura,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SemCandidaturaView(
    onCriarCandidatura: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ainda não submeteu nenhuma candidatura.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = candidatura.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Curso: ${candidatura.curso}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Nº Estudante: ${candidatura.num_estudante}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            EstadoCandidaturaBadge(estado = candidatura.estado)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Data de submissão: $dataSubmissao",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EstadoCandidaturaBadge(
    estado: EstadoCandidatura
) {
    val label = when (estado) {
        EstadoCandidatura.EM_ANALISE -> "Em análise"
        EstadoCandidatura.APROVADA -> "Aprovada"
        EstadoCandidatura.REJEITADA -> "Rejeitada"
    }

    AssistChip(
        onClick = { },
        label = { Text(label) }
    )
}
