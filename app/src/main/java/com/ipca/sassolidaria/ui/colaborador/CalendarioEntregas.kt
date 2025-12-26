package com.ipca.sassolidaria.ui.colaborador

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val PendingDot = Color(0xFFFFC107) // pendente
private val ReadyDot = Color(0xFF3B82F6)   // pronta

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarioEntregas(
    yearMonth: YearMonth,
    entregas: List<EntregaCalendarioUi>,
    onDiaClick: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()

    val startOffset = ((firstDay.dayOfWeek.value % 7)) // seg=1..dom=7 -> dom=0
    val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

    val byDay = entregas.groupBy { it.dia }

    val weekDays = listOf("D", "S", "T", "Q", "Q", "S", "S")

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        weekDays.forEach {
            Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }

    Spacer(Modifier.height(8.dp))

    for (i in 0 until totalCells step 7) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (j in 0..6) {
                val cell = i + j
                val dayNum = cell - startOffset + 1

                if (dayNum in 1..daysInMonth) {
                    val date = yearMonth.atDay(dayNum)
                    val items = byDay[date].orEmpty()
                    val hasPending = items.any { it.estado.name == "PENDENTE" }
                    val hasReady = items.any { it.estado.name == "PRONTA" }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Transparent)
                        ) {
                            Text(
                                dayNum.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(3.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (hasPending) Dot(PendingDot)
                            if (hasReady) Dot(ReadyDot)
                        }
                    }
                } else {
                    Spacer(Modifier.width(36.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}
