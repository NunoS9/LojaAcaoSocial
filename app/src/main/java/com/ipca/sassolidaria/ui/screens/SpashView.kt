package com.ipca.sassolidaria.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipca.sassolidaria.R

@Composable
fun SplashView(onNavigateToLogin: () -> Unit) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon_sassolidaria),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(250.dp)
                .clickable { onNavigateToLogin() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    SplashView(onNavigateToLogin = {})
}
