package com.app.kenala.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.kenala.screens.auth.LoginScreen
import com.app.kenala.screens.auth.OnboardingScreen
import com.app.kenala.screens.auth.RegisterScreen
import com.app.kenala.screens.journal.*
import com.app.kenala.screens.main.MainScreen
import com.app.kenala.screens.mission.*
import com.app.kenala.screens.notifications.NotificationsCenterScreen
import com.app.kenala.screens.profile.*
import com.app.kenala.screens.stats.StatisticsScreen
import com.app.kenala.viewmodel.AuthState
import com.app.kenala.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // State untuk menampilkan error dan loading
    var loginError by remember { mutableStateOf<String?>(null) }
    var registerError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Loading -> {
                isLoading = true
            }
            is AuthState.Success -> {
                isLoading = false
                loginError = null
                registerError = null
                // Navigate to main screen on success
                navController.navigate(Screen.Main.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                isLoading = false
                // Set error based on current screen
                if (navController.currentDestination?.route == Screen.Login.route) {
                    loginError = state.message
                } else if (navController.currentDestination?.route == Screen.Register.route) {
                    registerError = state.message
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Idle -> {
                isLoading = false
            }
        }
    }

    // Determine start destination based on login status
    val startDestination = if (isLoggedIn) Screen.Main.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination
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
            LaunchedEffect(Unit) {
                loginError = null
            }

            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                errorMessage = loginError,
                isLoading = isLoading
            )
        }

        composable(route = Screen.Register.route) {
            LaunchedEffect(Unit) {
                registerError = null
            }

            RegisterScreen(
                onRegisterClick = { name, email, password ->
                    authViewModel.register(name, email, password)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                errorMessage = registerError,
                isLoading = isLoading
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
            arguments = listOf(navArgument("journalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("journalId")
            if (journalId != null) {
                JournalDetailScreen(
                    journalId = journalId.toIntOrNull() ?: 0,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate("${Screen.EditJournal.route}/$id")
                    }
                )
            }
        }

        composable(
            route = "${Screen.EditJournal.route}/{journalId}",
            arguments = listOf(navArgument("journalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getString("journalId")
            if (journalId != null) {
                EditJournalScreen(
                    journalId = journalId.toIntOrNull() ?: 0,
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

        // ======== PROFILE SCREENS ========
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DailyStreak.route) {
            DailyStreakScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BadgeCollection.route) {
            BadgeCollectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DetailedStats.route) {
            DetailedStatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdventureSuggestion.route) {
            AdventureSuggestionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}