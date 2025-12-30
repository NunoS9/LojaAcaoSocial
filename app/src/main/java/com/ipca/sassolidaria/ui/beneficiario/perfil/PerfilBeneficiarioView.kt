package com.ipca.sassolidaria.ui.beneficiario.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipca.sassolidaria.R

private val Green = Color(0xFF004B3C)
private val RedButton = Color(0xFFE57373)
private val LightGrayLine = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilBeneficiarioView(
    viewModel: PerfilBeneficiarioViewModel = viewModel(),
    onLogoutConfirmed: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    suporteSlot: @Composable () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPreferenciasDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val ScreenBg = Color(0xFFF6F7FB)
    val DividerColor = Color(0xFFB0B0B0)

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ScreenBg)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Perfil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ScreenBg
                    )
                )
                HorizontalDivider(color = DividerColor, thickness = 2.dp)
            }
        },
        bottomBar = bottomBar) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.blue_user_circle),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(70.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(state.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Beneficiário SAS", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Text("Definições", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(14.dp))

            Divider(color = LightGrayLine)

            PerfilItem(
                icon = R.drawable.ic_user_circle,
                text = "Ver/Editar Dados Pessoais",
                onClick = { showEditDialog = true }
            )

            Divider(color = LightGrayLine)

            PerfilItem(
                icon = R.drawable.ic_calendar,
                text = "Preferências de Agendamento",
                onClick = { showPreferenciasDialog = true }
            )

            Divider(color = LightGrayLine)

            /* ---------- SUPORTE ---------- */
            suporteSlot()

            Divider(color = LightGrayLine)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedButton)
            ) {
                Text("Sair", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }

    // ---------- EDITAR ----------
    if (showEditDialog) {
        EditarDadosDialog(
            nomeInicial = state.nome,
            email = state.email,
            onCancel = { showEditDialog = false },
            onSave = { newName, ok, err -> viewModel.updateName(newName, ok, err) },
            onAutoClose = { showEditDialog = false }
        )
    }

    // ---------- PREFERÊNCIAS ----------
    if (showPreferenciasDialog) {
        PreferenciaDialogBeneficiario(
            state = state,
            onCancel = { showPreferenciasDialog = false },
            onToggleDia = { viewModel.toggleDia(it) },
            onHoraInicio = { viewModel.updateHoraInicio(it) },
            onHoraFim = { viewModel.updateHoraFim(it) },
            onObs = { viewModel.updateObservacoes(it) },
            onSave = { ok, err ->
                viewModel.guardarPreferencias(ok, err)
            }
        )
    }

    // ---------- LOGOUT ----------
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sair da conta") },
            text = { Text("Tem a certeza que deseja sair?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutConfirmed()
                }) { Text("SIM") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("NÃO") }
            }
        )
    }
}

@Composable
fun PerfilItem(icon: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = Green,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPerfilBeneficiario() {
    PerfilBeneficiarioView()
}
