package com.ipca.sassolidaria.ui.chat.components

import androidx.compose.runtime.Composable
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.ui.colaborador.perfil.PerfilItem

@Composable
fun SuportePerfilItem(
    onClick: () -> Unit
) {
    PerfilItem(
        icon = R.drawable.ic_support,
        text = "Suporte",
        onClick = onClick
    )
}
