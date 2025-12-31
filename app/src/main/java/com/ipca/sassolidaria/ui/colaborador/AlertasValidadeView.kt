package com.ipca.sassolidaria.ui.colaborador

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState

private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFE0E0E0)

private val ExpiredBg = Color(0xFFFFEBEE)     // vermelho muito suave
private val ExpiredText = Color(0xFFD32F2F)   // vermelho

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasValidadeView(
    navController: NavController,
    viewModel: AlertasValidadeViewModel = viewModel()
) {
    val state = viewModel.alertasState
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.start()
        viewModel.marcarComoVistos()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alertas de Validade",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(48.dp))
                }
                Divider(color = DividerColor)
            }
        },
        bottomBar = { BottomBar(navController = navController) },
        containerColor = ScreenBg
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.updateQuery(it)
                },
                placeholder = { Text("Procurar produtos por nome ou validade") },
                modifier = Modifier.fillMaxWidth()
            )

            when (state) {
                is ResultState.Loading, ResultState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ResultState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }

                is ResultState.Success -> {
                    val list = state.data
                    if (list.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sem alertas de validade.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(list, key = { it.idProduto }) { p ->
                                AlertaProdutoCard(p)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertaProdutoCard(p: AlertaProdutoUi) {
    val bg = if (p.isExpired) ExpiredBg else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    p.nome,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                if (p.isExpired) {
                    AssistChip(
                        onClick = { /* sem ação */ },
                        label = { Text("Expirado") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.Transparent,
                            labelColor = ExpiredText
                        ),
                        border = BorderStroke(1.dp, ExpiredText)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "Validade: ${p.validadeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = if (p.isExpired) ExpiredText else Color.Gray
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Quantidade em Stock: ${p.quantidadeDisponivel}",
                style = MaterialTheme.typography.bodySmall,
                color = if (p.isExpired) ExpiredText else Color.Unspecified
            )
        }
    }
}
