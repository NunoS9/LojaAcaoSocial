package com.ipca.sassolidaria.core

import com.ipca.sassolidaria.ui.components.BottomBar
import com.ipca.sassolidaria.ui.colaborador.perfil.PerfilColaboradorView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.ipca.sassolidaria.auth.LoginView
import com.ipca.sassolidaria.auth.RegisterView
import com.ipca.sassolidaria.auth.RecoverPasswordView
import com.ipca.sassolidaria.ui.screens.SplashView
import com.ipca.sassolidaria.ui.beneficiario.HomeBeneficiarioView
import com.ipca.sassolidaria.ui.colaborador.HomeColaboradorView
import com.ipca.sassolidaria.ui.utilizador.HomeUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.perfil.PerfilUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.NotificacoesUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.candidaturas.CandidaturasUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.candidaturas.CandidaturaCreateView
import com.ipca.sassolidaria.ui.beneficiario.perfil.PerfilBeneficiarioView
import com.ipca.sassolidaria.ui.beneficiario.pedidos.PedidosListView
import com.ipca.sassolidaria.ui.beneficiario.pedidos.PedidoCreateView
import com.ipca.sassolidaria.ui.beneficiario.pedidos.PedidoDetailsView
import com.ipca.sassolidaria.ui.colaborador.candidaturas.CandidaturasListView
import com.ipca.sassolidaria.ui.colaborador.candidaturas.CandidaturaDetailView
import com.ipca.sassolidaria.ui.colaborador.produtos.ProdutosListView
import com.ipca.sassolidaria.ui.colaborador.produtos.ProdutoEditView
import com.ipca.sassolidaria.ui.colaborador.produtos.ProdutoCreateView

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
            LoginView(navController = navController)
        }

        // REGISTER
        composable(Routes.REGISTER) {
            RegisterView(navController = navController)
        }

        // RECOVER PASSWORD
        composable(Routes.RECOVER_PASSWORD) {
            RecoverPasswordView(onBack = { navController.popBackStack() })
        }

        // -------- HOME COLABORADOR --------
        composable(Routes.HOME_COLABORADOR) {

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("userType", "COLABORADOR")

            HomeColaboradorView(navController = navController)
        }

        composable(Routes.PRODUTOS_LIST) {
            ProdutosListView(navController)
        }

        composable(Routes.PRODUTO_CREATE) {
            ProdutoCreateView(navController)
        }

        composable(Routes.PRODUTO_EDIT) { backStackEntry ->
            ProdutoEditView(navController = navController)
        }

        // PERFIL COLABORADOR
        composable(Routes.PERFIL_COLABORADOR) {

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("userType", "COLABORADOR")

            PerfilColaboradorView(
                viewModel = viewModel(),
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                },
                bottomBar = { BottomBar(navController = navController) }
            )
        }

        composable(Routes.CANDIDATURAS_LIST) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("userType", "COLABORADOR")

            CandidaturasListView(navController = navController)
        }

        // CANDIDATURAS (DETALHE) - COLABORADOR
        composable(
            route = "${Routes.CANDIDATURA_DETAIL_COLABORADOR}/{candidaturaId}",
            arguments = listOf(
                navArgument("candidaturaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Recuperar o ID passado na rota
            val id = backStackEntry.arguments?.getString("candidaturaId") ?: ""

            CandidaturaDetailView(
                navController = navController,
                candidaturaId = id
            )
        }

        // -------- HOME BENEFICIARIO --------
        composable(Routes.HOME_BENEFICIARIO) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("userType", "BENEFICIARIO")

            HomeBeneficiarioView(navController = navController)
        }

        // PERFIL BENEFICIARIO
        composable(Routes.PERFIL_BENEFICIARIO) {
            PerfilBeneficiarioView(
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                    }
                },
                bottomBar = { BottomBar(navController = navController) }
            )
        }



        // -------- HOME UTILIZADOR --------
        composable(Routes.HOME_UTILIZADOR) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("userType", "UTILIZADOR")

            HomeUtilizadorView(navController = navController)
        }
        composable(Routes.PERFIL_UTILIZADOR) {
            PerfilUtilizadorView(navController = navController)
        }

        composable(Routes.CANDIDATURA_UTILIZADOR) {
            CandidaturasUtilizadorView(navController = navController)
        }

        composable(Routes.CANDIDATURA_CREATE) {
            CandidaturaCreateView(navController = navController)
        }

        composable(Routes.NOTIFICACOES_UTILIZADOR) {
            NotificacoesUtilizadorView(navController = navController)
        }

        // PEDIDOS COLABORADOR
        composable(Routes.PEDIDOS_LIST) {
            PedidosListView(navController = navController)
        }

        composable(Routes.PEDIDOS_CREATE) {
            PedidoCreateView(navController = navController)
        }

        composable(
            route = "${Routes.PEDIDO_DETAILS_BENEFICIARIO}/{pedidoId}"
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
            PedidoDetailsView(
                navController = navController,
                pedidoId = pedidoId
            )
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
