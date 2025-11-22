package com.ipca.sassolidaria.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController


@Composable
fun App() {
    MaterialTheme {
        AppNavHost()
    }
}
