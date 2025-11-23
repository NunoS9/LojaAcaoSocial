package com.ipca.sassolidaria.ui.beneficiario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ipca.sassolidaria.core.UserType
import com.ipca.sassolidaria.ui.components.BottomBar

@Composable
fun HomeBeneficiarioView(navController: NavController) {

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Home do Beneficiário",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBeneficiarioPreview() {
    val nav = rememberNavController()
    nav.currentBackStackEntry?.savedStateHandle?.set("userType", UserType.BENEFICIARIO.name)

    HomeBeneficiarioView(navController = nav)
}
