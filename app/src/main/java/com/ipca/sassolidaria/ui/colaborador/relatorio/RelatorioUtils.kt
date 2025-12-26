package com.ipca.sassolidaria.ui.colaborador.relatorio

import java.util.Calendar
import java.util.Date

fun monthNamePt(month: Int): String = when (month) {
    0 -> "Janeiro"
    1 -> "Fevereiro"
    2 -> "Março"
    3 -> "Abril"
    4 -> "Maio"
    5 -> "Junho"
    6 -> "Julho"
    7 -> "Agosto"
    8 -> "Setembro"
    9 -> "Outubro"
    10 -> "Novembro"
    11 -> "Dezembro"
    else -> "Mês"
}

/** Calcula intervalo [start, end) para o mês/ano, ou nulls se allTime. */
fun calcularIntervaloRelatorio(month: Int, year: Int, allTime: Boolean): Pair<Date?, Date?> {
    if (allTime) return Pair(null, null)

    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
    }

    val start = cal.time
    cal.add(Calendar.MONTH, 1)
    val end = cal.time

    return Pair(start, end)
}
