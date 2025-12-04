package com.ipca.sassolidaria.ui.colaborador.produtos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Soap
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
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.data.model.Produto
import com.ipca.sassolidaria.data.model.enums.TipoProduto
import com.ipca.sassolidaria.ui.components.BottomBar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutosListView(
    navController: NavController,
    viewModel: ProdutosListViewModel = viewModel()
) {
    val state = viewModel.uiState

    // Se voltares do create/edit, garante refresh
    LaunchedEffect(Unit) {
        viewModel.carregarProdutos()
    }

    // filtro atual (null = todos)
    var filtro by remember { mutableStateOf<TipoProduto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gestão de Produtos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                // sem navigationIcon -> sem seta
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.PRODUTO_CREATE)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Adicionar produto"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F7FB))
        ) {

            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    val produtosFiltrados = state.produtos.filter { prod ->
                        filtro == null || prod.tipo_produto == filtro
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {

                        // Barra de filtros
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = filtro == null,
                                onClick = { filtro = null },
                                label = { Text("Todos") }
                            )
                            FilterChip(
                                selected = filtro == TipoProduto.ALIMENTACAO,
                                onClick = { filtro = TipoProduto.ALIMENTACAO },
                                label = { Text("Alimentação") }
                            )
                            FilterChip(
                                selected = filtro == TipoProduto.HIGIENE,
                                onClick = { filtro = TipoProduto.HIGIENE },
                                label = { Text("Higiene") }
                            )
                            FilterChip(
                                selected = filtro == TipoProduto.LIMPEZA,
                                onClick = { filtro = TipoProduto.LIMPEZA },
                                label = { Text("Limpeza") }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(produtosFiltrados) { produto ->
                                ProdutoGestaoCard(
                                    produto = produto,
                                    onEdit = {
                                        navController.navigate(
                                            Routes.PRODUTO_EDIT.replace("{id}", produto.id_produto)
                                        )
                                    },
                                    onDelete = { viewModel.apagarProduto(produto.id_produto) },
                                    tipoLabel = viewModel.tipoToLabel(produto.tipo_produto)
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
private fun iconForTipo(tipo: TipoProduto) = when (tipo) {
    TipoProduto.ALIMENTACAO -> Icons.Outlined.Restaurant
    TipoProduto.HIGIENE     -> Icons.Outlined.Soap
    TipoProduto.LIMPEZA     -> Icons.Outlined.CleaningServices
}

@Composable
private fun ProdutoGestaoCard(
    produto: Produto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    tipoLabel: String
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val validadeStr = produto.data_validade?.let { sdf.format(it) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF4F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForTipo(produto.tipo_produto),
                    contentDescription = null,
                    tint = Color(0xFF4F4F4F)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tipo: $tipoLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6F6F6F)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Quantidade: ${produto.quantidade_disponivel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6F6F6F)
                )
                validadeStr?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Validade: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6F6F6F)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF4F5F7))
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar"
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFE4E4))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}
