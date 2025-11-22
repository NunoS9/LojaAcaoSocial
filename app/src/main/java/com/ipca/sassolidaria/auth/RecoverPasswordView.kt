package com.ipca.sassolidaria.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipca.sassolidaria.R

@Composable
fun RecoverPasswordView(
    onBack: () -> Unit,
    viewModel: RecoverPasswordViewModel = viewModel()
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
            painter = painterResource(id = R.drawable.icon_sassolidaria),
            contentDescription = null,
            modifier = Modifier.size(250.dp)
        )

        Spacer(Modifier.height(25.dp))

        Text(
            text = "Recuperar Palavra-passe",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(30.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email associado à conta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // mensagem de erro
        state.errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        // mensagem de sucesso
        state.successMessage?.let {
            Text(
                text = it,
                color = Color(0xFF004B3C),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.recover {
                    // se quiseres voltar automaticamente depois de enviar:
                    // onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF004B3C)
            ),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text("Enviar instruções", color = Color.White, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Voltar", color = Color(0xFF004B3C))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecoverPreview() {
    RecoverPasswordView(onBack = {})
}
