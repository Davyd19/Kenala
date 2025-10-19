package com.app.kenala.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.kenala.screens.auth.LoginScreen
import com.app.kenala.screens.auth.OnboardingScreen
import com.app.kenala.screens.main.MainScreen
import com.app.kenala.screens.mission.GachaScreen
import com.app.kenala.screens.mission.GuidanceScreen


/**
 * Ini adalah NavGraph utama aplikasi.
 * Ia bertugas menentukan layar mana yang ditampilkan berdasarkan rute.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {

    // NavHost adalah "panggung" yang akan menampilkan layar-layar kita
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route // Layar pertama yang dibuka
    ) {

        // Rute untuk Layar Onboarding
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStartedClick = {
                    // Pergi ke Login & hapus Onboarding dari tumpukan (back stack)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Rute untuk Layar Login
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    // Pergi ke Layar Utama & hapus Login dari tumpukan
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    // TODO: Nanti kita bisa arahkan ke Screen.Register.route
                }
            )
        }

        composable(route = Screen.Main.route) {
            MainScreen(navController = navController)
        }
        // Rute untuk Layar Gacha (Pencarian Misi)
        composable(route = Screen.Gacha.route) {
            GachaScreen(
                onMissionFound = {
                    // Pergi ke Layar Petunjuk & hapus Gacha dari tumpukan
                    navController.navigate(Screen.Guidance.route) {
                        popUpTo(Screen.Gacha.route) { inclusive = true }
                    }
                }
            )
        }

        // Rute untuk Layar Gacha (Pencarian Misi)
        composable(route = Screen.Gacha.route) {
            GachaScreen(
                onMissionFound = {
                    // Pergi ke Layar Petunjuk & hapus Gacha dari tumpukan
                    navController.navigate(Screen.Guidance.route) {
                        popUpTo(Screen.Gacha.route) { inclusive = true }
                    }
                }
            )
        }

        // Rute untuk Layar Panduan Misi
        composable(route = Screen.Guidance.route) {
            GuidanceScreen(
                onCancelClick = {
                    // Kembali ke Home
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onArrivedClick = {
                    // Pergi ke Layar Tiba (Arrival)
                    navController.navigate(Screen.Arrival.route) {
                        popUpTo(Screen.Guidance.route) { inclusive = true }
                    }
                }
            )
        }

    }
}