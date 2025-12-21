package com.app.kenala

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- SUBSCRIBE KE TOPIC 'all_users' (Untuk Notifikasi Broadcast) ---
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to all_users"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("FCM", msg)
            }
        // -------------------------------------------------------------------

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