package com.ipca.sassolidaria.ui.beneficiario.perfil

import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipca.sassolidaria.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Green = Color(0xFF004B3C)
private val RedButton = Color(0xFFE57373)
private val LightGrayLine = Color(0xFFE0E0E0)
private val Light = Color(0xFFF2F7F5)
private val GreyInput = Color(0xFFEDEDED)

@Composable
fun PerfilBeneficiarioView(
    viewModel: PerfilBeneficiarioViewModel = viewModel(),
    onLogoutConfirmed: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPreferenciasDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(bottomBar = bottomBar) { padding ->

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
                icon = R.drawable.ic_key,
                text = "Alterar Senha",
                onClick = { showPasswordDialog = true }
            )

            Divider(color = LightGrayLine)

            PerfilItem(
                icon = R.drawable.ic_calendar,
                text = "Preferências de Agendamento",
                onClick = { showPreferenciasDialog = true }
            )

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

    // ---------- ALTERAR SENHA ----------
    if (showPasswordDialog) {
        AlterarSenhaDialog(
            onCancel = { showPasswordDialog = false },
            onSave = { atual, nova, ok, err ->
                viewModel.updatePassword(atual, nova, ok, err)
            },
            onAutoClose = { showPasswordDialog = false }
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
fun AlterarSenhaDialog(
    onCancel: () -> Unit,
    onSave: (String, String, () -> Unit, (String) -> Unit) -> Unit,
    onAutoClose: () -> Unit
) {
    var atual by remember { mutableStateOf("") }
    var nova by remember { mutableStateOf("") }
    var success by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text("Alterar Senha") },
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
                    value = atual,
                    onValueChange = { atual = it },
                    label = { Text("Password Atual") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nova,
                    onValueChange = { nova = it },
                    label = { Text("Nova Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        atual,
                        nova,
                        {
                            error = ""
                            success = "Senha alterada com sucesso!"
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

@Preview(showBackground = true)
@Composable
fun PreviewPerfilBeneficiario() {
    PerfilBeneficiarioView()
}
