package com.ipca.sassolidaria.ui.utilizador.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.data.firebase.FirestoreSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.core.Routes
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Green = Color(0xFF004B3C)
private val LightGrayLine = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUtilizadorView(navController: NavController) {
    // Obtém o userId do utilizador autenticado no Firebase
    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: return // Se não houver usuário autenticado, retorna

    // Estado para armazenar os dados do utilizador
    var userName by remember { mutableStateOf("Carregando...") }
    var userType by remember { mutableStateOf("Carregando...") }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para controlar os diálogos
    var showEditDialog by remember { mutableStateOf(false) }  // Estado para o diálogo de edição
    var showPasswordDialog by remember { mutableStateOf(false) }

    val viewModel: PerfilUtilizadorViewModel = viewModel() // Usando o ViewModel

    // Buscar os dados do utilizador na base de dados Firestore
    LaunchedEffect(userId) {
        val firestoreSource = FirestoreSource()
        val utilizador = withContext(Dispatchers.IO) {
            firestoreSource.getUtilizadorById(userId)
        }

        if (utilizador != null) {
            userName = utilizador.nome
            userType = utilizador.tipo.name // Acessa o tipo de utilizador, e.g., "UTILIZADOR"
        } else {
            userName = "Usuário não encontrado"
            userType = "Tipo não disponível"
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meu Perfil") }
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Exibe mensagem de "Carregando..." enquanto os dados estão sendo carregados
            if (isLoading) {
                Text("Carregando...", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // CARD com avatar + nome + tipo de utilizador
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Avatar redondo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Avatar",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userName, // Agora o nome vem da base de dados
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userType, // Exibe o tipo de utilizador em vez do email
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Título da secção
            Text(
                text = "Definições",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))
            Divider(color = LightGrayLine)

            // Opção: Ver/Editar Dados Pessoais
            PerfilItem(
                icon = R.drawable.ic_user_circle,
                text = "Ver/Editar Dados Pessoais",
                onClick = { showEditDialog = true }
            )

            Divider(color = LightGrayLine)

            // Opção: Alterar Senha
            PerfilItem(
                icon = R.drawable.ic_key,
                text = "Alterar Senha",
                onClick = { showPasswordDialog = true }
            )

            Divider(color = LightGrayLine)
            Spacer(modifier = Modifier.weight(1f))

            // Botão SAIR (Logout)
            Button(
                onClick = {
                    // Faz o logout do utilizador
                    FirebaseAuth.getInstance().signOut()
                    // Navega para a tela de login
                    navController.navigate(Routes.LOGIN) {
                        // Quando fizer logout, limpar a pilha de navegação
                        popUpTo(Routes.HOME_UTILIZADOR) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text("Sair", fontSize = 16.sp)
            }
        }
    }

    // Acessar o estado do ViewModel para editar dados pessoais
    val state = viewModel.uiState.collectAsState().value

    // Diálogo para Editar Dados Pessoais
    if (showEditDialog) {
        EditarDadosDialog(
            nomeInicial = state.nome,
            emailInicial = state.email,
            onCancel = { showEditDialog = false },
            onSave = { nome, email, success, error ->
                viewModel.editUserData(
                    nome,
                    email,
                    onSuccess = { success() },
                    onError = { error() }
                )
            },
            onAutoClose = { showEditDialog = false }
        )
    }

    // Diálogo para Alterar Senha
    if (showPasswordDialog) {
        AlterarSenhaDialog(
            onCancel = { showPasswordDialog = false },
            onSave = { pass, success, error ->
                viewModel.updatePassword(
                    pass,
                    onSuccess = { success() },
                    onError = { error() }
                )
            },
            onAutoClose = { showPasswordDialog = false }
        )
    }
}

// Função para os itens de opções de perfil
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

// Função para o diálogo de editar dados pessoais
@Composable
fun EditarDadosDialog(
    nomeInicial: String,
    emailInicial: String,
    onCancel: () -> Unit,
    onSave: (String, String, () -> Unit, () -> Unit) -> Unit,
    onAutoClose: () -> Unit
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var email by remember { mutableStateOf(emailInicial) }

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
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {

                onSave(
                    nome,
                    email,
                    {
                        error = ""
                        success = "Dados alterados com sucesso!"

                        scope.launch {
                            delay(1500)
                            onAutoClose()
                        }
                    },
                    {
                        error = "Preencha todos os campos!"
                        success = ""
                    }
                )

            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                success = ""
                error = ""
                onCancel()
            }) {
                Text("Cancelar")
            }
        }
    )
}

// Função para o diálogo de alterar senha
@Composable
fun AlterarSenhaDialog(
    onCancel: () -> Unit,
    onSave: (String, () -> Unit, () -> Unit) -> Unit,
    onAutoClose: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    var success by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            success = ""
            error = ""
            onCancel()
        },
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
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nova Senha") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {

                onSave(
                    password,
                    {
                        error = ""
                        success = "Senha alterada com sucesso!"

                        scope.launch {
                            delay(1500)
                            onAutoClose()
                        }
                    },
                    {
                        error = "Insira uma senha válida!"
                        success = ""
                    }
                )

            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                success = ""
                error = ""
                onCancel()
            }) { Text("Cancelar") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PerfilUtilizadorPreview() {
    val nav = rememberNavController()
    PerfilUtilizadorView(navController = nav)
}
