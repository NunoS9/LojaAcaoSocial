package com.ipca.sassolidaria.ui.colaborador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsNone
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
import com.ipca.sassolidaria.data.model.Notificacao
import com.ipca.sassolidaria.data.model.enums.TipoNotificacao
import com.ipca.sassolidaria.ui.components.BottomBar

private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)
private val GreenDark = Color(0xFF004B3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesColaboradorView(
    navController: NavController,
    viewModel: NotificacoesColaboradorViewModel = viewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Notificações",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg,
                        scrolledContainerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        },
        bottomBar = { BottomBar(navController = navController) },
        containerColor = ScreenBg
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScreenBg)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage ?: "Erro",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.notificacoes.isEmpty() -> {
                    Text(
                        text = "Sem notificações.",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.notificacoes, key = { it.id_notificacao }) { notif ->
                            NotificacaoCardColaborador(notif)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacaoCardColaborador(notificacao: Notificacao) {
    val categoria = when (notificacao.tipo) {
        TipoNotificacao.NOVA_CANDIDATURA -> "Candidaturas"
        TipoNotificacao.NOVO_PEDIDO -> "Pedidos"
        TipoNotificacao.AGENDAMENTO_ATUALIZADO -> "Agendamentos"
        TipoNotificacao.NOVO_AGENDAMENTO -> "Agendamentos"
        TipoNotificacao.PEDIDO_ATUALIZADO -> "Pedidos"
        TipoNotificacao.ENTREGA_ATUALIZADA -> "Entregas"
        TipoNotificacao.LEMBRETE_ENTREGA -> "Entregas"
        TipoNotificacao.CANDIDATURA_ATUALIZADA -> "Candidaturas"
        TipoNotificacao.GERAL -> "Geral"
    }

    val tempo = notificacao.data_envio?.toString() ?: ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                    tint = GreenDark
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    text = categoria,
                    color = GreenDark,
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
                text = notificacao.conteudo,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6F6F6F)
            )
        }
    }
}
