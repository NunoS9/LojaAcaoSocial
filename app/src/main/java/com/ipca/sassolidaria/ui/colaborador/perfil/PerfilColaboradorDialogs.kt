package com.ipca.sassolidaria.ui.colaborador.perfil


import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ipca.sassolidaria.ui.colaborador.relatorio.PerfilColaboradorRelatorioViewModel
import com.ipca.sassolidaria.ui.colaborador.relatorio.RelatorioDialog
import com.ipca.sassolidaria.ui.colaborador.relatorio.calcularIntervaloRelatorio
import com.ipca.sassolidaria.ui.colaborador.relatorio.monthNamePt
import com.ipca.sassolidaria.utils.PdfReportGenerator
import com.ipca.sassolidaria.utils.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

private val Green = Color(0xFF004B3C)

@Composable
fun EditarDadosDialog(
    nomeInicial: String,
    email: String,
    onCancel: () -> Unit,
    onSave: (String, () -> Unit, (String) -> Unit) -> Unit,
    onAutoClose: () -> Unit
) {
    var nome by remember { mutableStateOf(nomeInicial) }

    var success by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            success = ""
            error = ""
            onCancel()
        },
        title = { Text("Editar Dados") },
        text = {
            Column {
                if (success.isNotEmpty()) {
                    Text(success, color = Color(0xFF2E7D32))
                    Spacer(Modifier.height(10.dp))
                }

                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red)
                    Spacer(Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledBorderColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        nome,
                        {
                            error = ""
                            success = "Nome atualizado!"
                            scope.launch {
                                delay(1500)
                                onAutoClose()
                            }
                        },
                        { msg ->
                            success = ""
                            error = msg
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) { Text("Cancelar") }
        }
    )
}


@Composable
fun RelatorioDialogHost(
    relatorioVm: PerfilColaboradorRelatorioViewModel,
    initialMonth: Int,
    initialYear: Int,
    initialAllTime: Boolean,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit,
    onPersistSelection: (Int, Int, Boolean) -> Unit
) {
    val context = LocalContext.current

    RelatorioDialog(
        initialMonth = initialMonth,
        initialYear = initialYear,
        initialAllTime = initialAllTime,
        onDismiss = onDismiss,
        onConfirm = { month, year, allTime ->
            onPersistSelection(month, year, allTime)

            val (start, end) = calcularIntervaloRelatorio(month, year, allTime)

            onStart()

            relatorioVm.carregarRelatorioCompleto(start, end) { result ->
                when (result) {
                    is ResultState.Loading -> Unit

                    is ResultState.Error -> {
                        onError(result.message)
                    }

                    is ResultState.Success -> {
                        val r = result.data
                        val c = r.contagens

                        val report = PdfReportGenerator.ReportData(
                            title = "Relatório - Loja Social",
                            subtitle = if (allTime) "Filtro: Tudo" else "Filtro: ${monthNamePt(month)} $year",
                            generatedAt = Date(),
                            blocks = listOf(
                                PdfReportGenerator.Block.SectionTitle("Resumo"),
                                PdfReportGenerator.Block.Paragraph(
                                    listOf(
                                        "Pedidos pendentes: ${c.pedidosPendentes}",
                                        "Pedidos entregues: ${c.pedidosEntregues}",
                                        "Pedidos cancelados: ${c.pedidosCancelados}",
                                        "Entregas pendentes: ${c.entregasPendentes}",
                                        "Entregas prontas: ${c.entregasProntas}",
                                        "Entregas entregues: ${c.entregasEntregues}",
                                        "Entregas canceladas: ${c.entregasCanceladas}",
                                        "Candidaturas em análise: ${c.candidaturasEmAnalise}",
                                        "Candidaturas aprovadas: ${c.candidaturasAprovadas}",
                                        "Candidaturas rejeitadas: ${c.candidaturasRejeitadas}"
                                    )
                                ),
                                PdfReportGenerator.Block.Table(
                                    title = "Pedidos",
                                    headers = listOf("Nome", "Estado", "Data"),
                                    rows = r.pedidosRows
                                ),
                                PdfReportGenerator.Block.Table(
                                    title = "Entregas",
                                    headers = listOf("Beneficiário", "Estado", "Tipo", "Agendamento"),
                                    rows = r.entregasRows
                                ),
                                PdfReportGenerator.Block.Table(
                                    title = "Stock atual",
                                    headers = listOf("Produto", "Disponível", "Total"),
                                    rows = r.stockRows
                                )
                            )
                        )

                        val uri = PdfReportGenerator.generateAndSave(context, report)

                        if (uri == null) {
                            onError("Não foi possível guardar o PDF.")
                            return@carregarRelatorioCompleto
                        }

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Abrir relatório"))

                        onSuccess()
                    }

                    else -> Unit
                }
            }
        }
    )
}
