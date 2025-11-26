package com.ipca.sassolidaria.ui.utilizador.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.ipca.sassolidaria.core.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUtilizadorView(navController: NavController) {
    // Obtém o userId do utilizador autenticado no Firebase
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Se não houver usuário autenticado, retorna

    // Estado para armazenar os dados do utilizador
    var userName by remember { mutableStateOf("Carregando...") }
    var userType by remember { mutableStateOf("Carregando...") }
    var isLoading by remember { mutableStateOf(true) }

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

            // Opção: Ver/Editar Dados Pessoais
            ProfileOptionRow(
                icon = Icons.Outlined.Edit,
                text = "Ver/Editar Dados Pessoais",
                onClick = {
                    // TODO: navegar para ecrã de edição do perfil
                }
            )

            Divider()

            // Opção: Alterar Senha
            ProfileOptionRow(
                icon = Icons.Outlined.Lock,
                text = "Alterar Senha",
                onClick = {
                    // TODO: navegar para ecrã de alteração de password
                }
            )

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
}

@Composable
private fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF004B3C),
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp
        )

        Icon(
            imageVector = Icons.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilUtilizadorPreview() {
    val nav = rememberNavController()
    PerfilUtilizadorView(navController = nav)
}
