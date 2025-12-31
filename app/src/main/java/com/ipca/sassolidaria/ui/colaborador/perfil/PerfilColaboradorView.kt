package com.ipca.sassolidaria.ui.colaborador.perfil

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
import com.ipca.sassolidaria.ui.colaborador.relatorio.PerfilColaboradorRelatorioViewModel

private val ScreenBg = Color(0xFFF6F7FB)
private val DividerColor = Color(0xFFB0B0B0)
private val Green = Color(0xFF004B3C)
private val RedButton = Color(0xFFE57373)
private val LightGrayLine = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilColaboradorView(
    viewModel: PerfilColaboradorViewModel = viewModel(),
    onLogoutConfirmed: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    suporteSlot: @Composable () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Relatório VM
    val relatorioVm: PerfilColaboradorRelatorioViewModel = viewModel()

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }


    var showReportDialog by remember { mutableStateOf(false) }
    var generatingReport by remember { mutableStateOf(false) }
    var reportError by remember { mutableStateOf<String?>(null) }

    val calNow = remember { java.util.Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(calNow.get(java.util.Calendar.MONTH)) } // 0..11
    var selectedYear by remember { mutableIntStateOf(calNow.get(java.util.Calendar.YEAR)) }
    var reportAllTime by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBg,
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
        bottomBar = bottomBar
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

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
                    Text("Colaborador SAS", fontSize = 14.sp, color = Color.Gray)
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
                icon = R.drawable.ic_report,
                text = "Gerar Relatório (PDF)",
                onClick = { if (!generatingReport) showReportDialog = true }
            )

            if (generatingReport) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("A gerar relatório...", fontSize = 13.sp, color = Color.Gray)
                }
            }

            if (reportError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = reportError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Divider(color = LightGrayLine)

            suporteSlot()

            Divider(color = LightGrayLine)

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedButton)
            ) {
                Text("Sair", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }

    // --------- DIALOGS (agora num ficheiro separado) ---------

    if (showEditDialog) {
        EditarDadosDialog(
            nomeInicial = state.nome,
            email = state.email,
            onCancel = { showEditDialog = false },
            onSave = { nomeNovo, success, error ->
                viewModel.updateName(
                    newName = nomeNovo,
                    onSuccess = { success() },
                    onError = { msg -> error(msg) }
                )
            },
            onAutoClose = { showEditDialog = false }
        )
    }

    if (showReportDialog) {
        RelatorioDialogHost(
            relatorioVm = relatorioVm,
            initialMonth = selectedMonth,
            initialYear = selectedYear,
            initialAllTime = reportAllTime,
            onDismiss = { showReportDialog = false },
            onStart = {
                generatingReport = true
                reportError = null
            },
            onError = { msg ->
                generatingReport = false
                reportError = msg
            },
            onSuccess = {
                generatingReport = false
            },
            onPersistSelection = { month, year, allTime ->
                selectedMonth = month
                selectedYear = year
                reportAllTime = allTime
            }
        )
    }

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
fun PreviewPerfilColaboradorView() {
    PerfilColaboradorView()
}
