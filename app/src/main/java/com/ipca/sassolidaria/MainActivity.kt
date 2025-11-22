package com.ipca.sassolidaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ipca.sassolidaria.core.AppNavHost
import com.ipca.sassolidaria.ui.theme.LojaAcaoSocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LojaAcaoSocialTheme {
                AppNavHost()
            }
        }
    }
}

