package com.app.kenala.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.kenala.screens.auth.LoginScreen
import com.app.kenala.screens.auth.OnboardingScreen
import com.app.kenala.screens.auth.RegisterScreen
import com.app.kenala.screens.journal.EditJournalScreen
import com.app.kenala.screens.journal.JournalDetailScreen
import com.app.kenala.screens.journal.JournalEntryScreen
import com.app.kenala.screens.main.MainScreen
import com.app.kenala.screens.mission.GachaScreen
import com.app.kenala.screens.mission.GuidanceScreen
import com.app.kenala.screens.mission.MissionPreferencesScreen
import com.app.kenala.screens.notifications.NotificationsCenterScreen
import com.app.kenala.screens.profile.EditProfileScreen
import com.app.kenala.screens.stats.StatisticsScreen
import com.app.kenala.screens.profile.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {

        // ======== AUTH SCREENS ========
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ======== MAIN SCREEN ========
        composable(route = Screen.Main.route) {
            MainScreen(navController = navController)
        }

        // ======== MISSION FLOW ========
        composable(route = Screen.MissionPreferences.route) {
            MissionPreferencesScreen(
                onNavigateToGacha = {
                    navController.navigate(Screen.Gacha.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
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
                onGiveUpClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = true
                        }
                    }
                },
                onArrivedClick = {
                    navController.navigate(Screen.JournalEntry.route)
                }
            )
        }

        // ======== JOURNAL FLOW ========
        composable(route = Screen.JournalEntry.route) {
            JournalEntryScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.JournalDetail.route}/{journalId}",
            arguments = listOf(navArgument("journalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getInt("journalId")
            if (journalId != null) {
                JournalDetailScreen(
                    journalId = journalId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate("${Screen.EditJournal.route}/$id")
                    }
                )
            }
        }

        composable(
            route = "${Screen.EditJournal.route}/{journalId}",
            arguments = listOf(navArgument("journalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getInt("journalId")
            if (journalId != null) {
                EditJournalScreen(
                    journalId = journalId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() },
                    onDeleteClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ======== STATISTICS ========
        composable(route = Screen.Statistics.route) {
            StatisticsScreen(onBackClick = { navController.popBackStack() })
        }

        // ======== NOTIFICATIONS ========
        composable(route = Screen.Notifications.route) {
            NotificationsCenterScreen(onBackClick = { navController.popBackStack() })
        }

        composable(route = Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}