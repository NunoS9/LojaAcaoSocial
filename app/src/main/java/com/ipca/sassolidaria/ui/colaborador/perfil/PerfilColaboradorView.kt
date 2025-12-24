package com.ipca.sassolidaria.ui.colaborador.perfil

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

@Composable
fun PerfilColaboradorView(
    viewModel: PerfilColaboradorViewModel = viewModel(),
    onLogoutConfirmed: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    suporteSlot: @Composable () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(bottomBar = bottomBar) { padding ->

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
                icon = R.drawable.ic_key,
                text = "Alterar Senha",
                onClick = { showPasswordDialog = true }
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

    // ---------- EDITAR DADOS ----------
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

    // ---------- ALTERAR PASSWORD ----------
    if (showPasswordDialog) {
        AlterarSenhaDialog(
            onCancel = { showPasswordDialog = false },
            onSave = { passAtual, novaPass, success, error ->

                viewModel.updatePassword(
                    currentPassword = passAtual,
                    newPassword = novaPass,
                    onSuccess = { success() },
                    onError = { msg -> error(msg) }
                )
            },
            onAutoClose = { showPasswordDialog = false }
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
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
fun AlterarSenhaDialog(
    onCancel: () -> Unit,
    onSave: (String, String, () -> Unit, (String) -> Unit) -> Unit,
    onAutoClose: () -> Unit
) {
    var passwordAtual by remember { mutableStateOf("") }
    var novaPassword by remember { mutableStateOf("") }

    var success by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text("Alterar Senha") },
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
                    value = passwordAtual,
                    onValueChange = { passwordAtual = it },
                    label = { Text("Password Atual") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = novaPassword,
                    onValueChange = { novaPassword = it },
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
                        passwordAtual,
                        novaPassword,
                        {
                            error = ""
                            success = "Senha alterada com sucesso!"
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

@Preview(showBackground = true)
@Composable
fun PreviewPerfilColaboradorView() {
    PerfilColaboradorView()
}
