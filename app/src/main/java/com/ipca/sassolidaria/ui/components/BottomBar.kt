package com.ipca.sassolidaria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.core.Routes
import com.ipca.sassolidaria.core.UserType

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

private val ActiveGreen = Color(0xFF004B3C)
private val InactiveGray = Color(0xFF9E9E9E)
private val ActiveBubble = Color(0xFFE7ECF6)

data class BottomNavItem(
    val label: String,
    val route: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

@Composable
fun BottomBar(navController: NavController) {

    val userTypeStr = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("userType")
        ?: "UTILIZADOR"

    val userType = UserType.valueOf(userTypeStr)

    val items = when (userType) {

        UserType.COLABORADOR -> listOf(
            BottomNavItem("Início", Routes.HOME_COLABORADOR, Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem("Produtos", Routes.PRODUTOS_LIST, Icons.Filled.Inventory, Icons.Outlined.Inventory),
            BottomNavItem("Doações", Routes.DOACOES_LIST, Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism),
            BottomNavItem("Candidaturas", Routes.CANDIDATURAS_LIST, Icons.Filled.List, Icons.Outlined.List),
            BottomNavItem("Perfil", Routes.PERFIL_COLABORADOR, Icons.Filled.Person, Icons.Outlined.Person),
        )

        UserType.BENEFICIARIO -> listOf(
            BottomNavItem("Início", Routes.HOME_BENEFICIARIO, Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem("Pedidos", Routes.PEDIDOS_LIST, Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
            BottomNavItem("Entregas", Routes.ENTREGAS_LIST, Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping),
            BottomNavItem("Marcações", Routes.MARCACOES_LIST, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
            BottomNavItem("Perfil", Routes.PERFIL_BENEFICIARIO, Icons.Filled.Person, Icons.Outlined.Person),
        )

        UserType.UTILIZADOR -> listOf(
            BottomNavItem("Início", Routes.HOME_UTILIZADOR, Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem("Perfil", Routes.PERFIL_BENEFICIARIO, Icons.Filled.Person, Icons.Outlined.Person),
            BottomNavItem("Candidaturas", Routes.CANDIDATURAS_LIST, Icons.Filled.List, Icons.Outlined.List),
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->

            val selected = currentRoute == item.route

            Column(
                modifier = Modifier
                    .width(70.dp)
                    .clickable {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Ícone + bolha arredondada
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (selected) ActiveBubble else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.label,
                        tint = if (selected) ActiveGreen else InactiveGray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Label
                Text(
                    text = item.label,
                    color = if (selected) ActiveGreen else InactiveGray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
