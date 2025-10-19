package com.app.kenala.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.kenala.screens.auth.LoginScreen
import com.app.kenala.screens.auth.OnboardingScreen
import com.app.kenala.screens.auth.RegisterScreen
import com.app.kenala.screens.main.MainScreen
import com.app.kenala.screens.mission.GachaScreen
import com.app.kenala.screens.mission.GuidanceScreen
import com.app.kenala.screens.mission.MissionPreferencesScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(route = Screen.MissionPreferences.route) {
            MissionPreferencesScreen(
                onNavigateToGacha = {
                    navController.navigate(Screen.Gacha.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Gacha.route) {
            GachaScreen(
                onMissionFound = {
                    navController.navigate(Screen.Guidance.route) {
                        popUpTo(Screen.Gacha.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Guidance.route) {
            GuidanceScreen(
                onCancelClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onArrivedClick = {
                    // TODO: Arahkan ke ArrivalScreen
                }
            )
        }
    }
}

