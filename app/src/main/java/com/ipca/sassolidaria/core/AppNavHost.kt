package com.ipca.sassolidaria.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

import com.ipca.sassolidaria.ui.components.BottomBar

import com.ipca.sassolidaria.auth.LoginView
import com.ipca.sassolidaria.auth.RegisterView
import com.ipca.sassolidaria.auth.RecoverPasswordView

import com.ipca.sassolidaria.ui.screens.SplashView

import com.ipca.sassolidaria.ui.colaborador.HomeColaboradorView
import com.ipca.sassolidaria.ui.colaborador.perfil.PerfilColaboradorView
import com.ipca.sassolidaria.ui.colaborador.candidaturas.CandidaturasListView
import com.ipca.sassolidaria.ui.colaborador.candidaturas.CandidaturaDetailView
import com.ipca.sassolidaria.ui.colaborador.produtos.ProdutosListView
import com.ipca.sassolidaria.ui.colaborador.produtos.ProdutoCreateView
import com.ipca.sassolidaria.ui.colaborador.produtos.ProdutoEditView
import com.ipca.sassolidaria.ui.colaborador.doacoes.DoacoesListView
import com.ipca.sassolidaria.ui.colaborador.doacoes.DoacaoCreateView
import com.ipca.sassolidaria.ui.colaborador.doacoes.DoacaoDetailView

import com.ipca.sassolidaria.ui.beneficiario.HomeBeneficiarioView
import com.ipca.sassolidaria.ui.beneficiario.perfil.PerfilBeneficiarioView
import com.ipca.sassolidaria.ui.beneficiario.pedidos.PedidosListView
import com.ipca.sassolidaria.ui.beneficiario.pedidos.PedidoCreateView
import com.ipca.sassolidaria.ui.beneficiario.pedidos.PedidoDetailsView
import com.ipca.sassolidaria.ui.beneficiario.marcacoes.MarcacoesBeneficiarioView
import com.ipca.sassolidaria.ui.beneficiario.entregas.EntregasListView


import com.ipca.sassolidaria.ui.utilizador.HomeUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.perfil.PerfilUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.NotificacoesUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.candidaturas.CandidaturasUtilizadorView
import com.ipca.sassolidaria.ui.utilizador.candidaturas.CandidaturaCreateView


@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        /* ------------------------- SPLASH ------------------------- */
        composable(Routes.SPLASH) {
            SplashView(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        /* ------------------------- AUTENTICAÇÃO ------------------------- */
        composable(Routes.LOGIN) { LoginView(navController) }
        composable(Routes.REGISTER) { RegisterView(navController) }
        composable(Routes.RECOVER_PASSWORD) {
            RecoverPasswordView(onBack = { navController.popBackStack() })
        }

        /* ------------------------- HOME COLABORADOR ------------------------- */
        composable(Routes.HOME_COLABORADOR) {
            navController.currentBackStackEntry?.savedStateHandle?.set("userType", "COLABORADOR")
            HomeColaboradorView(navController)
        }

        /* ------------------------- PRODUTOS ------------------------- */
        composable(Routes.PRODUTOS_LIST) { ProdutosListView(navController) }
        composable(Routes.PRODUTO_CREATE) { ProdutoCreateView(navController) }
        composable(Routes.PRODUTO_EDIT) { ProdutoEditView(navController) }

        /* ------------------------- PERFIL COLABORADOR ------------------------- */
        composable(Routes.PERFIL_COLABORADOR) {
            navController.currentBackStackEntry?.savedStateHandle?.set("userType", "COLABORADOR")
            PerfilColaboradorView(
                viewModel = viewModel(),
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                bottomBar = { BottomBar(navController) }
            )
        }

        /* ------------------------- DOAÇÕES ------------------------- */
        composable(Routes.DOACOES_LIST) {
            navController.currentBackStackEntry?.savedStateHandle?.set("userType", "COLABORADOR")

            Scaffold(
                bottomBar = { BottomBar(navController) }
            ) { padding ->
                DoacoesListView(
                    navController = navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Routes.DOACAO_CREATE) { DoacaoCreateView(navController) }

        composable(
            route = Routes.DOACAO_DETAIL + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            DoacaoDetailView(
                navController = navController,
                id = entry.arguments?.getString("id") ?: ""
            )
        }

        /* ------------------------- CANDIDATURAS (COLABORADOR) ------------------------- */
        composable(Routes.CANDIDATURAS_LIST) {
            navController.currentBackStackEntry?.savedStateHandle?.set("userType", "COLABORADOR")
            CandidaturasListView(navController)
        }

        composable(
            route = Routes.CANDIDATURA_DETAIL_COLABORADOR + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            CandidaturaDetailView(
                navController = navController,
                candidaturaId = entry.arguments?.getString("id") ?: ""
            )
        }

        /* ------------------------- BENEFICIÁRIO ------------------------- */
        composable(Routes.HOME_BENEFICIARIO) {
            navController.currentBackStackEntry?.savedStateHandle?.set("userType", "BENEFICIARIO")
            HomeBeneficiarioView(navController)
        }

        composable(Routes.PERFIL_BENEFICIARIO) {
            PerfilBeneficiarioView(
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                bottomBar = { BottomBar(navController) }
            )
        }

        composable(Routes.PEDIDOS_LIST) { PedidosListView(navController) }
        composable(Routes.PEDIDOS_CREATE) { PedidoCreateView(navController) }

        composable(
            route = Routes.PEDIDO_DETAILS_BENEFICIARIO + "/{pedidoId}",
            arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
        ) { entry ->
            PedidoDetailsView(navController, entry.arguments?.getString("pedidoId") ?: "")
        }

        composable(Routes.MARCACOES_LIST) {
            MarcacoesBeneficiarioView(navController = navController)
        }

        composable(Routes.ENTREGAS_LIST) {
            EntregasListView(navController = navController)
        }

        /* ------------------------- HOME UTILIZADOR ------------------------- */
        composable(Routes.HOME_UTILIZADOR) {
            navController.currentBackStackEntry?.savedStateHandle?.set("userType", "UTILIZADOR")
            HomeUtilizadorView(navController)
        }

        composable(Routes.PERFIL_UTILIZADOR) { PerfilUtilizadorView(navController) }
        composable(Routes.CANDIDATURA_UTILIZADOR) { CandidaturasUtilizadorView(navController) }
        composable(Routes.CANDIDATURA_CREATE) { CandidaturaCreateView(navController) }
        composable(Routes.NOTIFICACOES_UTILIZADOR) { NotificacoesUtilizadorView(navController) }
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
