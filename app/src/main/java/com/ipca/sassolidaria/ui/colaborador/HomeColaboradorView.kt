package com.ipca.sassolidaria.ui.colaborador

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.WarningAmber
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
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.utils.ResultState
import java.time.LocalDate
import java.time.YearMonth

private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFF858585)
private val GreenDark = Color(0xFF004B3C)
private val AlertBg = Color(0xFFE3F3ED)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeColaboradorView(
    navController: NavController,
    viewModel: HomeColaboradorViewModel = viewModel()
) {
    val entregasState = viewModel.entregasState
    val unseenAlerts = viewModel.unseenAlertasCount
    val alertasLoading = viewModel.alertasCountLoading

    val unseenNotifs = viewModel.unseenNotificacoesCount
    val notifsLoading = viewModel.notificacoesCountLoading

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
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
            Spacer(Modifier.height(12.dp))

            // --- BOTÕES: Notificações + Alertas ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeTopButton(
                    icon = Icons.Outlined.NotificationsNone,
                    label = "Notificações",
                    badgeCount = if (notifsLoading) null else unseenNotifs,
                    onClick = { navController.navigate(Routes.NOTIFICACOES_COLABORADOR) }
                )

                HomeTopButton(
                    icon = Icons.Outlined.WarningAmber,
                    label = "Alertas de Validade",
                    badgeCount = if (alertasLoading) null else unseenAlerts,
                    onClick = { navController.navigate(Routes.ALERTAS_VALIDADE_COLABORADOR) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // --- CALENDÁRIO + LISTA ENTREGAS ---
            Text(
                text = "Entregas",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            when (entregasState) {
                is ResultState.Loading, ResultState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ResultState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(entregasState.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is ResultState.Success -> {
                    val entregas = entregasState.data
                    val hoje = LocalDate.now()
                    val ym = YearMonth.of(hoje.year, hoje.month)

                    // Calendário
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(10.dp))

                            CalendarioEntregas(
                                yearMonth = ym,
                                entregas = entregas,
                                onDiaClick = { /* opcional */ }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (entregas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Não existem entregas pendentes/prontas.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(entregas, key = { it.idEntrega }) { e ->
                                EntregaResumoCard(e)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeCount: Int?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AlertBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(icon, contentDescription = label, tint = GreenDark)
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = GreenDark
            )

            Spacer(Modifier.weight(1f))

            if (badgeCount != null && badgeCount > 0) {
                Badge(containerColor = GreenDark) {
                    Text(badgeCount.toString(), color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EntregaResumoCard(e: EntregaCalendarioUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = e.nomeBeneficiario.ifBlank { "Beneficiário" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                EstadoEntregaChipMini(e.estadoLabel, e.estadoColorBg, e.estadoColorFg)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = e.dataHoraLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Entrega #${e.idEntrega}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun EstadoEntregaChipMini(label: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
