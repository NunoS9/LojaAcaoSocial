package com.ipca.sassolidaria.ui.utilizador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.ui.components.BottomBar
import androidx.compose.foundation.clickable

// Dados “mock” só para o layout do Home
data class HomeProdutoItem(
    val nome: String,
    val quantidade: Int,
    val tipo: String
)

@Composable
fun HomeUtilizadorView(navController: NavController) {

    // Lista de exemplo igual ao mockup
    val produtos = listOf(
        HomeProdutoItem("Arroz Basmati (1kg)", 50, "Alimentação"),
        HomeProdutoItem("Azeite Virgem Extra (750ml)", 15, "Alimentação"),
        HomeProdutoItem("Leite UHT (1L)", 80, "Alimentação")
    )

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F7FB))   // fundo clarinho
        ) {

            // TÍTULO "Home"
            Text(
                text = "Home",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // BARRA "Notificações" com ícone de sino
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F3ED))   // verde claro tipo mockup
                    .clickable {
                        navController.navigate(com.ipca.sassolidaria.core.Routes.NOTIFICACOES_UTILIZADOR)
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notificações",
                        tint = Color(0xFF004B3C)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Notificações",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF004B3C)
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // TÍTULO DA LISTA
            Text(
                text = "Todos os Itens em Stock",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            // LISTA DE CARDS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(produtos) { produto ->
                    ProdutoStockCard(produto)
                }
            }
        }
    }
}

@Composable
private fun ProdutoStockCard(item: HomeProdutoItem) {
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

            // Ícone tipo talheres
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF4F5F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = "Produto",
                    tint = Color(0xFF4F4F4F)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${item.quantidade} disponível",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6F6F6F)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Tipo: ${item.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6F6F6F)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeUtilizadorPreview() {
    val nav = rememberNavController()
    // para a BottomBar saber que é utilizador
    nav.currentBackStackEntry?.savedStateHandle?.set("userType", UserType.UTILIZADOR.name)

    HomeUtilizadorView(navController = nav)
}
