package com.app.kenala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.app.kenala.navigation.AppNavGraph
import com.app.kenala.ui.theme.KenalaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 1. Mengaktifkan mode Edge-to-Edge (layar penuh)
        // Ini membuat aplikasi Anda tampil di belakang status bar
        enableEdgeToEdge()

        setContent {
            // 2. Terapkan tema kustom Anda (dari Langkah 5)
            KenalaTheme {

                // 3. Buat NavController untuk mengelola navigasi
                val navController = rememberNavController()

                // 4. Panggil AppNavGraph (dari Langkah 7)
                // Ini akan menjadi "panggung" untuk semua layar Anda
                AppNavGraph(navController = navController)
            }
        }
    }
}