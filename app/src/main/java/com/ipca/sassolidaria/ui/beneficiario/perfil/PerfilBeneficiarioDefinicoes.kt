package com.ipca.sassolidaria.ui.beneficiario.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Green = Color(0xFF004B3C)
private val Light = Color(0xFFF2F7F5)
private val GreyInput = Color(0xFFEDEDED)

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
        onDismissRequest = { onCancel() },
        title = { Text("Editar Dados") },
        text = {
            Column {

                if (success.isNotEmpty()) {
                    Text(success, color = Green)
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
                        { msg -> error = msg }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
fun PreferenciaDialogBeneficiario(
    state: PerfilBeneficiarioState,
    onCancel: () -> Unit,
    onToggleDia: (String) -> Unit,
    onHoraInicio: (String) -> Unit,
    onHoraFim: (String) -> Unit,
    onObs: (String) -> Unit,
    onSave: (() -> Unit, (String) -> Unit) -> Unit,
) {
    var success by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var horaInicioFocused by remember { mutableStateOf(false) }
    var horaFimFocused by remember { mutableStateOf(false) }
    var obsFocused by remember { mutableStateOf(false) }

    val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.95f),
        onDismissRequest = { onCancel() },
        title = { Text("Preferências de Agendamento") },
        text = {
            Column(Modifier.fillMaxWidth()) {

                if (success.isNotEmpty()) {
                    Text(success, color = Green)
                    Spacer(Modifier.height(10.dp))
                }

                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red)
                    Spacer(Modifier.height(10.dp))
                }

                Text("Dia(s) da Semana", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dias.chunked(3).forEach { linha ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            linha.forEach { dia ->
                                val selected = dia in state.diasSelecionados

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (selected) Light else GreyInput,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) Green else Color.LightGray,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onToggleDia(dia) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = dia,
                                        color = if (selected) Green else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Horário Preferencial", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    OutlinedTextField(
                        value = state.horaInicio,
                        onValueChange = onHoraInicio,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged {
                                if (it.isFocused && !horaInicioFocused) {
                                    horaInicioFocused = true
                                    onHoraInicio("")
                                }
                            },
                        placeholder = { Text("09:00") }
                    )

                    OutlinedTextField(
                        value = state.horaFim,
                        onValueChange = onHoraFim,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged {
                                if (it.isFocused && !horaFimFocused) {
                                    horaFimFocused = true
                                    onHoraFim("")
                                }
                            },
                        placeholder = { Text("17:00") }
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text("Observações", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = state.observacoes,
                    onValueChange = onObs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .onFocusChanged {
                            if (it.isFocused && !obsFocused) {
                                obsFocused = true
                                onObs("")
                            }
                        },
                    placeholder = {
                        Text("Adicione quaisquer observações...", color = Color.Gray)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        {
                            error = ""
                            success = "Preferências enviadas com sucesso!"
                            scope.launch {
                                delay(1800)
                                onCancel()
                            }
                        },
                        { msg ->
                            success = ""
                            error = msg
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Preferência", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}
