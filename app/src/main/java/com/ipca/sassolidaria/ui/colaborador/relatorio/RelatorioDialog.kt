package com.ipca.sassolidaria.ui.colaborador.relatorio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ipca.sassolidaria.R
private val Green = Color(0xFF004B3C)


@Composable
fun RelatorioDialog(
    initialMonth: Int,
    initialYear: Int,
    initialAllTime: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int, allTime: Boolean) -> Unit
) {
    var month by remember { mutableIntStateOf(initialMonth.coerceIn(0, 11)) }
    var year by remember { mutableIntStateOf(initialYear) }
    var allTime by remember { mutableStateOf(initialAllTime) }

    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gerar relatório") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = allTime, onCheckedChange = { allTime = it })
                    Text("Tudo (sem filtro de mês)")
                }

                if (!allTime) {
                    Text("Mês", style = MaterialTheme.typography.labelMedium)

                    SelectorRow(
                        centerText = monthNamePt(month),
                        onPrev = { month = (month + 11) % 12 },
                        onNext = { month = (month + 1) % 12 }
                    )

                    Text("Ano", style = MaterialTheme.typography.labelMedium)

                    SelectorRow(
                        centerText = year.toString(),
                        onPrev = { year -= 1 },
                        onNext = { year += 1 },
                        prevEnabled = year > currentYear - 10,
                        nextEnabled = year < currentYear
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = { onConfirm(month, year, allTime) },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Gerar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }

    )
}

@Composable
private fun SelectorRow(
    centerText: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    prevEnabled: Boolean = true,
    nextEnabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev, enabled = prevEnabled) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Anterior",
                modifier = Modifier.rotate(180f)
            )
        }

        Text(
            text = centerText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(onClick = onNext, enabled = nextEnabled) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Seguinte"
            )
        }
    }
}
