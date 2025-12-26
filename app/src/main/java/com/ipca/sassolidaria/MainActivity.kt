package com.ipca.sassolidaria

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.ipca.sassolidaria.core.AppNavHost
import com.ipca.sassolidaria.ui.theme.LojaAcaoSocialTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LojaAcaoSocialTheme {
                AppNavHost()
            }
        }
    }
}

