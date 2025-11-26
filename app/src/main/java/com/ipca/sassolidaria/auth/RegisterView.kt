package com.ipca.sassolidaria.auth
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.core.Routes

@Composable
fun RegisterView(
    navController: NavController? = null,
    viewModel: RegisterViewModel = viewModel()
) {
    val state = viewModel.uiState


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(60.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(Modifier.height(25.dp))

        Text(
            text = "Registo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Crie aqui a sua conta para aceder ao sistema.",
            fontSize = 15.sp,
            color = Color.Black,
        )
        Spacer(Modifier.height(30.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Palavra-passe") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(26.dp))

        Button(
            onClick = {
                viewModel.register {
                    navController?.navigate(Routes.LOGIN)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF004B3C)
            )
        ) {
            Text("Registar", color = Color.White, fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))

        if (state.errorMessage.isNotEmpty()) {
            Text(state.errorMessage, color = Color.Red)
        }

        if (state.successMessage.isNotEmpty()) {
            Text(state.successMessage, color = Color.Green)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = { navController?.navigate(Routes.LOGIN) }
        ) {
            Text("Já tem conta? Iniciar sessão", color = Color(0xFF004B3C))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    RegisterView()
}
