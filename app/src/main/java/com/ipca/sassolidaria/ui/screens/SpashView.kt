package com.ipca.sassolidaria.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipca.sassolidaria.R
import kotlinx.coroutines.delay

@Composable
fun SplashView(onNavigateToLogin: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(2500)   // espera 2.5 segundos
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sas),
            contentDescription = "App Icon",
            modifier = Modifier.size(450.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    SplashView(onNavigateToLogin = {})
}
