package com.ipca.sassolidaria.ui.colaborador.doacoes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.Doacao
import com.ipca.sassolidaria.data.model.ItemDoacao
import java.text.SimpleDateFormat
import java.util.*

private val Branco = Color.White

@Composable
fun DoacoesListView(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: DoacoesListViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.loadDoacoes() }

    val lista by vm.doacoes.collectAsState()
    val itensPorDoacao by vm.itensPorDoacao.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        /* ---------- TOP BAR (SEM VOLTAR ATRÁS) ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Espaço para alinhar título ao centro
            Spacer(modifier = Modifier.width(48.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Doações", style = MaterialTheme.typography.titleLarge)
            }

            IconButton(onClick = {
                navController.navigate(Routes.DOACAO_CREATE)
            }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF0B5D47)
                )
            }
        }

        Divider()
        Spacer(Modifier.height(16.dp))

        /* ---------- LISTA DE DOAÇÕES ---------- */
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(lista) { doacao ->

                val itens = itensPorDoacao[doacao.id_doacao] ?: emptyList()

                DoacaoCard(
                    doacao = doacao,
                    itens = itens
                ) {
                    navController.navigate("${Routes.DOACAO_DETAIL}/${doacao.id_doacao}")
                }
            }
        }
    }
}

@Composable
fun DoacaoCard(
    doacao: Doacao,
    itens: List<ItemDoacao>,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Branco),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(Modifier.weight(1f)) {

                Text(doacao.descricao, style = MaterialTheme.typography.titleMedium)

                Text(
                    text = doacao.data_doacao?.let { sdf.format(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(6.dp))

                if (itens.isNotEmpty()) {
                    val resumo = itens.joinToString(", ") {
                        "${it.quantidade}x ${it.nome}"
                    }
                    Text(
                        resumo,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DoacoesListViewPreview() {
    val nav = rememberNavController()

    Scaffold(
        bottomBar = { com.ipca.sassolidaria.ui.components.BottomBar(navController = nav) }
    ) { padding ->
        DoacoesListView(
            navController = nav,
            modifier = Modifier.padding(padding)
        )
    }
}
