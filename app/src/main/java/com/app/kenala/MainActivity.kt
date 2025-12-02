package com.app.kenala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.app.kenala.navigation.AppNavGraph
import com.app.kenala.ui.theme.KenalaTheme
import com.app.kenala.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()

            val isDarkMode by settingsViewModel.darkModeEnabled.collectAsState(
                initial = isSystemInDarkTheme()
            )

            KenalaTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}