package com.ipca.sassolidaria.ui.utilizador.perfil

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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.core.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS ---
private val Green = Color(0xFF004B3C)
private val RedButton = Color(0xFFE57373)
private val LightGrayLine = Color(0xFFE0E0E0)

@Composable
fun PerfilUtilizadorView(
    navController: NavController,
    viewModel: PerfilUtilizadorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // --- HEADER ---
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
                    Text("Utilizador SAS", fontSize = 14.sp, color = Color.Gray)
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

            Spacer(Modifier.weight(1f))

            // --- LOGOUT BUTTON ---
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
                    onError = { error(it) }
                )
            },
            onAutoClose = { showEditDialog = false }
        )
    }

    // ---------- ALTERAR PASSWORD ----------
    if (showPasswordDialog) {
        AlterarSenhaDialog(
            onCancel = { showPasswordDialog = false },
            onSave = { atual, nova, success, error ->

                viewModel.updatePassword(
                    newPassword = nova,
                    onSuccess = { success() },
                    onError = { error(it) }
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
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME_UTILIZADOR) { inclusive = true }
                        }
                    }
                ) { Text("SIM") }
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
        modifier = Modifier.fillMaxWidth()
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

// ------------------------------
//      EDITAR DADOS DIALOG
// ------------------------------
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
        onDismissRequest = onCancel,
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
                    readOnly = true,
                    enabled = false,
                    label = { Text("Email") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.Gray,
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
                            success = "Nome atualizado!"
                            error = ""
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
                shape = RoundedCornerShape(12.dp) //  BOTÃO NÃO RETANGULAR
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

// ------------------------------
//      ALTERAR PASSWORD DIALOG
// ------------------------------
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
        onDismissRequest = onCancel,
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
                    value = atual,
                    onValueChange = { atual = it },
                    label = { Text("Password Atual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nova,
                    onValueChange = { nova = it },
                    label = { Text("Nova Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
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
                            success = "Senha alterada!"
                            error = ""
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
                shape = RoundedCornerShape(12.dp) //  BOTÃO NÃO RETANGULAR
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPerfilUtilizador() {
    val nav = rememberNavController()
    PerfilUtilizadorView(navController = nav)
}
