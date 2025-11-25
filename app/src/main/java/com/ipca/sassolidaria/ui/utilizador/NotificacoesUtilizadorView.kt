package com.ipca.sassolidaria.ui.utilizador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.core.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesUtilizadorView(
    navController: NavController,
    viewModel: NotificacoesUtilizadorViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notificações",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F7FB))
        ) {

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.errorMessage, color = Color.Red)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.notificacoes) { notif ->
                        NotificacaoCard(notificacao = notif)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacaoCard(notificacao: Notificacao) {

    // converter Date para texto (por agora placeholder)
    val tempo = notificacao.data_envio?.toString() ?: "Agora"

    // converter enum para texto bonito
    val categoria = when (notificacao.tipo) {
        TipoNotificacao.CONFIRMACAO -> "Geral"
        TipoNotificacao.LEMBRETE_ENTREGA -> "Urgente"
        TipoNotificacao.VALIDADE_PRODUTO -> "Validade"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = Color(0xFF004B3C)
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    text = categoria,
                    color = Color(0xFF004B3C),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = tempo,
                    color = Color(0xFF8A8A8A),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = notificacao.titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = notificacao.conteudo,   // <-- CORRIGIDO
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6F6F6F)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ver Detalhes",
                color = Color(0xFF004B3C),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NotificacoesUtilizadorPreview() {
    val nav = rememberNavController()
    nav.currentBackStackEntry?.savedStateHandle?.set("userType", UserType.UTILIZADOR.name)
    NotificacoesUtilizadorView(navController = nav)
}
