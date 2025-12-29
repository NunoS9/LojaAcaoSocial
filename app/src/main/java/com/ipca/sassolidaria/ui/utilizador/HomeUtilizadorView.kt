package com.ipca.sassolidaria.ui.utilizador

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ipca.sassolidaria.ui.components.BottomBar

private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUtilizadorView(
    navController: NavController,
    viewModel: HomeUtilizadorViewModel = viewModel()
) {
    val state = viewModel.uiState
    val unseen = viewModel.unseenNotificacoesCount
    val loadingBadge = viewModel.notificacoesCountLoading

    LaunchedEffect(Unit) {
        viewModel.carregarNotificacoesUnseenCount()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = { Text("Home", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg,
                        scrolledContainerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) },
        containerColor = ScreenBg
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScreenBg)
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F3ED))
                    .clickable { navController.navigate(Routes.NOTIFICACOES_UTILIZADOR) },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notificações", tint = Color(0xFF004B3C))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Notificações",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF004B3C)
                        )
                    )

                    Spacer(Modifier.weight(1f))

                    if (!loadingBadge && unseen > 0) {
                        Badge(containerColor = Color(0xFF004B3C)) {
                            Text(unseen.toString(), color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Todos os Itens em Stock",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage!!, color = Color.Red)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.produtos) { produto ->
                            ProdutoStockCard(produto, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProdutoStockCard(
    item: Produto,
    viewModel: HomeUtilizadorViewModel
) {
    val icon = viewModel.getIconForTipo(item.tipo_produto)
    val tipoLabel = viewModel.tipoToLabel(item.tipo_produto)

    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF4F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = "Produto", tint = Color(0xFF4F4F4F))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.nome, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text("Quantidade: ${item.quantidade_disponivel}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F6F6F))
                Spacer(Modifier.height(2.dp))
                Text("Tipo: $tipoLabel", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F6F6F))
            }
        }
    }
}
