package com.ipca.sassolidaria.auth

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.core.Routes

@Composable
fun LoginView(
    navController: NavController?,
    viewModel: LoginViewModel = LoginViewModel()
) {

    val state = viewModel.uiState.value
    val greenPrimary = Color(0xFF004B3C)

    Surface(modifier = Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(60.dp))

            Image(
                painter = painterResource(R.drawable.icon_sassolidaria),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(30.dp))

            // EMAIL INPUT
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.uiState.value = state.copy(email = it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // PASSWORD INPUT
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.uiState.value = state.copy(password = it) },
                label = { Text("Palavra-passe") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // BOTÃO LOGIN
            Button(
                onClick = { viewModel.login { /* EXEMPLO: Vamos navegar depois */ } },
                colors = ButtonDefaults.buttonColors(containerColor = greenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Entrar", color = Color.White)
            }

            Spacer(Modifier.height(20.dp))

            // MENSAGENS DE ERRO / SUCESSO
            if (state.errorMessage.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    color = Color.Red,
                    fontSize = 15.sp
                )
            }

            if (state.successMessage.isNotEmpty()) {
                Text(
                    text = state.successMessage,
                    color = Color.Green,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Esqueceu a palavra-passe?",
                color = greenPrimary,
                modifier = Modifier.clickable {
                    navController?.navigate(Routes.RECOVER_PASSWORD)
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Ainda não tem conta? Registar-se",
                color = greenPrimary,
                modifier = Modifier.clickable {
                    navController?.navigate(Routes.REGISTER)
                }
            )
        }
    }
}
