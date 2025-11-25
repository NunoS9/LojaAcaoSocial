package com.ipca.sassolidaria.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.auth.LoginView
import com.ipca.sassolidaria.auth.RegisterView
import com.ipca.sassolidaria.auth.RecoverPasswordView
import com.ipca.sassolidaria.ui.screens.SplashView
import com.ipca.sassolidaria.ui.beneficiario.HomeBeneficiarioView
import com.ipca.sassolidaria.ui.colaborador.HomeColaboradorView
import com.ipca.sassolidaria.ui.utilizador.HomeUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.NotificacoesUtilizadorView

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
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
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
                navController = navController
            )
        }

        // RECOVER PASSWORD
        composable(Routes.RECOVER_PASSWORD) {
            RecoverPasswordView(
                onBack = { navController.popBackStack() }
            )
        }

        // -------- NOVAS ROTAS DE HOME --------
        composable(Routes.HOME_COLABORADOR) {
            HomeColaboradorView(navController = navController)
        }

        composable(Routes.HOME_BENEFICIARIO) {
            HomeBeneficiarioView(navController = navController)
        }

        composable(Routes.HOME_UTILIZADOR) {
            HomeUtilizadorView(navController = navController)
        }

        composable(Routes.NOTIFICACOES_UTILIZADOR) {
            NotificacoesUtilizadorView(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AppNavHost()
    }
}
