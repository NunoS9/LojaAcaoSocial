package com.ipca.sassolidaria.core

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.auth.LoginView
import com.ipca.sassolidaria.auth.RegisterView
import com.ipca.sassolidaria.auth.RecoverPasswordView
import com.ipca.sassolidaria.ui.screens.SplashView

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        // SPLASH
        composable(Routes.SPLASH) {
            SplashView(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        // LOGIN
        composable(Routes.LOGIN) {
            LoginView(
                navController = navController
            )
        }

        // REGISTER
        composable(Routes.REGISTER) {
            RegisterView(
                onBack = { navController.popBackStack() }
            )
        }

        // RECOVER PASSWORD
        composable(Routes.RECOVER_PASSWORD) {
            RecoverPasswordView(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
