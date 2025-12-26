package com.ipca.sassolidaria.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.google.firebase.auth.FirebaseAuth
import com.ipca.sassolidaria.ui.components.BottomBar

import com.ipca.sassolidaria.auth.LoginView
import com.ipca.sassolidaria.auth.RegisterView
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
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.BeneficiariosListView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.BeneficiarioDetailView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.PedidosBeneficiarioView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.ApoiosBeneficiarioView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.AgendamentoMarcarView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.BeneficiarioAgendamentosView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.AgendamentoEditarView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.BeneficiarioEntregasView
import com.ipca.sassolidaria.ui.colaborador.beneficiarios.ProcessarEntregaView

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

//  CHAT
import com.ipca.sassolidaria.ui.chat.ChatView
import com.ipca.sassolidaria.ui.chat.ChatListView
import com.ipca.sassolidaria.ui.chat.components.SuportePerfilItem

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    fun setUser(type: String) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set("userType", type)
    }

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
        composable(Routes.RECOVER_PASSWORD) { }

        /* ------------------------- COLABORADOR ------------------------- */

        composable(Routes.HOME_COLABORADOR) {
            setUser("COLABORADOR")
            HomeColaboradorView(navController)
        }

        composable(Routes.PRODUTOS_LIST) {
            setUser("COLABORADOR")
            ProdutosListView(navController)
        }

        composable(Routes.PRODUTO_CREATE) {
            setUser("COLABORADOR")
            ProdutoCreateView(navController)
        }

        composable(Routes.PRODUTO_EDIT) {
            setUser("COLABORADOR")
            ProdutoEditView(navController)
        }

        composable(Routes.PERFIL_COLABORADOR) {
            setUser("COLABORADOR")
            PerfilColaboradorView(
                viewModel = viewModel(),
                bottomBar = { BottomBar(navController) },
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                suporteSlot = {
                    SuportePerfilItem {
                        navController.navigate(Routes.CHAT_COLABORADOR_LIST)
                    }
                }
            )
        }

        composable(Routes.DOACOES_LIST) {
            setUser("COLABORADOR")
            Scaffold(bottomBar = { BottomBar(navController) }) { padding ->
                DoacoesListView(navController, Modifier.padding(padding))
            }
        }

        composable(Routes.DOACAO_CREATE) {
            setUser("COLABORADOR")
            DoacaoCreateView(navController)
        }

        composable(
            Routes.DOACAO_DETAIL + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            setUser("COLABORADOR")
            DoacaoDetailView(navController, entry.arguments?.getString("id") ?: "")
        }

        composable(Routes.CANDIDATURAS_LIST) {
            setUser("COLABORADOR")
            CandidaturasListView(navController)
        }

        composable(
            Routes.CANDIDATURA_DETAIL_COLABORADOR + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            setUser("COLABORADOR")
            CandidaturaDetailView(navController, entry.arguments?.getString("id") ?: "")
        }

        composable(Routes.BENEFICIARIOS_LIST) {
            setUser("COLABORADOR")
            Scaffold(bottomBar = { BottomBar(navController) }) { padding ->
                BeneficiariosListView(navController, Modifier.padding(padding))
            }
        }

        composable(
            Routes.BENEFICIARIO_DETAIL + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            setUser("COLABORADOR")
            BeneficiarioDetailView(navController, entry.arguments?.getString("id") ?: "")
        }

        composable(Routes.BENEFICIARIO_PEDIDOS) { backStack ->
            setUser("COLABORADOR")
            val idBenef = backStack.arguments?.getString("idBeneficiario") ?: ""
            PedidosBeneficiarioView(navController, idBenef)
        }

        composable(Routes.BENEFICIARIO_APOIOS) { backStack ->
            setUser("COLABORADOR")
            val idBenef = backStack.arguments?.getString("idBeneficiario") ?: ""
            ApoiosBeneficiarioView(navController, idBenef)
        }

        composable(Routes.AGENDAMENTO_MARCAR) { backStack ->
            setUser("COLABORADOR")
            val idBenef = backStack.arguments?.getString("idBeneficiario") ?: ""
            val tipo = backStack.arguments?.getString("tipo") ?: "PEDIDO"
            val ref = backStack.arguments?.getString("ref") ?: ""
            AgendamentoMarcarView(navController, idBenef, tipo, ref)
        }

        composable(Routes.BENEFICIARIO_AGENDAMENTOS) { backStackEntry ->
            setUser("COLABORADOR")
            val idBeneficiario = backStackEntry.arguments?.getString("idBeneficiario") ?: ""
            BeneficiarioAgendamentosView(navController, idBeneficiario)
        }

        composable("agendamento_editar/{idBeneficiario}/{idAgendamento}") { backStackEntry ->
            setUser("COLABORADOR")
            val idBeneficiario = backStackEntry.arguments?.getString("idBeneficiario") ?: ""
            val idAgendamento = backStackEntry.arguments?.getString("idAgendamento") ?: ""
            AgendamentoEditarView(navController, idBeneficiario, idAgendamento)
        }

        composable(
            route = Routes.BENEFICIARIO_ENTREGAS,
            arguments = listOf(navArgument("idBeneficiario") { type = NavType.StringType })
        ) { backStackEntry ->
            setUser("COLABORADOR")
            val idBeneficiario = backStackEntry.arguments?.getString("idBeneficiario").orEmpty()
            BeneficiarioEntregasView(navController, idBeneficiario)
        }

        composable(
            route = Routes.ENTREGA_PROCESSAR,
            arguments = listOf(
                navArgument("idEntrega") { type = NavType.StringType },
                navArgument("idBeneficiario") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            setUser("COLABORADOR")
            val idEntrega = backStackEntry.arguments?.getString("idEntrega").orEmpty()
            val idBeneficiario = backStackEntry.arguments?.getString("idBeneficiario").orEmpty()
            ProcessarEntregaView(navController, idEntrega, idBeneficiario)
        }

        /* ------------------------- BENEFICIÁRIO ------------------------- */

        composable(Routes.HOME_BENEFICIARIO) {
            setUser("BENEFICIARIO")
            HomeBeneficiarioView(navController)
        }

        composable(Routes.PERFIL_BENEFICIARIO) {
            setUser("BENEFICIARIO")
            PerfilBeneficiarioView(
                bottomBar = { BottomBar(navController) },
                onLogoutConfirmed = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                suporteSlot = {
                    SuportePerfilItem {
                        navController.navigate(Routes.CHAT_BENEFICIARIO)
                    }
                }
            )
        }

        composable(Routes.PEDIDOS_LIST) {
            setUser("BENEFICIARIO")
            PedidosListView(navController)
        }

        composable(Routes.PEDIDOS_CREATE) {
            setUser("BENEFICIARIO")
            PedidoCreateView(navController)
        }

        composable(
            Routes.PEDIDO_DETAILS_BENEFICIARIO + "/{pedidoId}",
            arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
        ) { entry ->
            setUser("BENEFICIARIO")
            PedidoDetailsView(navController, entry.arguments?.getString("pedidoId") ?: "")
        }

        composable(Routes.MARCACOES_LIST) {
            setUser("BENEFICIARIO")
            MarcacoesBeneficiarioView(navController)
        }

        composable(Routes.ENTREGAS_LIST) {
            setUser("BENEFICIARIO")
            EntregasListView(navController)
        }

        /* ------------------------- CHAT ------------------------- */

        composable(Routes.CHAT_BENEFICIARIO) {
            setUser("BENEFICIARIO")
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@composable
            ChatView(uid, UserType.BENEFICIARIO, navController)
        }

        composable(Routes.CHAT_COLABORADOR_LIST) {
            setUser("COLABORADOR")
            ChatListView(navController)
        }

        composable(
            route = Routes.CHAT_COLABORADOR,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStack ->
            setUser("COLABORADOR")
            val chatId = backStack.arguments?.getString("chatId") ?: return@composable
            ChatView(chatId, UserType.COLABORADOR, navController)
        }

        /* ------------------------- UTILIZADOR ------------------------- */

        composable(Routes.HOME_UTILIZADOR) {
            setUser("UTILIZADOR")
            HomeUtilizadorView(navController)
        }

        composable(Routes.PERFIL_UTILIZADOR) {
            setUser("UTILIZADOR")
            PerfilUtilizadorView(navController = navController)
        }

            composable(Routes.CANDIDATURA_UTILIZADOR) {
            setUser("UTILIZADOR")
            CandidaturasUtilizadorView(navController)
        }

        composable(Routes.CANDIDATURA_CREATE) {
            setUser("UTILIZADOR")
            CandidaturaCreateView(navController)
        }

        composable(Routes.NOTIFICACOES_UTILIZADOR) {
            setUser("UTILIZADOR")
            NotificacoesUtilizadorView(navController)
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
