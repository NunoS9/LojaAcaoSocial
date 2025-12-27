package com.ipca.sassolidaria.ui.colaborador.doacoes

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
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
private val GreenSas = Color(0xFF004B3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoacoesListView(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: DoacoesListViewModel = viewModel()
) {
    LaunchedEffect(Unit) { vm.loadDoacoes() }

    val lista by vm.doacoes.collectAsState()
    val itensPorDoacao by vm.itensPorDoacao.collectAsState()
    val ScreenBg = Color(0xFFF6F7FB)
    val DividerColor = Color(0xFFB0B0B0)

    Scaffold(
        modifier = modifier,
        containerColor = ScreenBg,
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Doações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(Routes.DOACAO_CREATE)
                        }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Criar",
                                tint = Color(0xFF0B5D47)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // O padding lateral fica SÓ no conteúdo
        ) {
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
