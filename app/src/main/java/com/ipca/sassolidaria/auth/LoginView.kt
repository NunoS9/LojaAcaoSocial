package com.ipca.sassolidaria.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.R
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.core.UserType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock

@Composable
fun LoginView(
    navController: NavController? = null,
    viewModel: LoginViewModel = remember { LoginViewModel() }
) {
    val state = viewModel.uiState
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
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Bem-vindo de volta!",
                fontSize = 22.sp,
                color = greenPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(30.dp))

            // EMAIL
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email",
                        tint = Color.Gray
                    )
                }
            )

            Spacer(Modifier.height(16.dp))

            // PASSWORD with SHOW/HIDE
            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Palavra-passe") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Password",
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    val desc = if (passwordVisible) "Ocultar palavra-passe" else "Mostrar palavra-passe"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = desc)
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            // BOTÃO LOGIN
            Button(
                onClick = {
                    viewModel.login { tipo ->

                        val route = when (tipo) {
                            UserType.COLABORADOR -> Routes.HOME_COLABORADOR
                            UserType.BENEFICIARIO -> Routes.HOME_BENEFICIARIO
                            UserType.UTILIZADOR -> Routes.HOME_UTILIZADOR
                        }

                        navController?.navigate(route) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }

                        navController?.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("userType", tipo.name)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Entrar", color = Color.White)
            }

            Spacer(Modifier.height(20.dp))

            if (state.errorMessage.isNotEmpty()) {
                Text(state.errorMessage, color = Color.Red, fontSize = 15.sp)
            }

            if (state.successMessage.isNotEmpty()) {
                Text(state.successMessage, color = Color.Green, fontSize = 15.sp)
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
@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    val nav = rememberNavController()
    LoginView(navController = nav)
}
