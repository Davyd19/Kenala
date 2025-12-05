package com.app.kenala.navigation

import androidx.compose.runtime.*
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
import com.app.kenala.viewmodel.AuthState
import com.app.kenala.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var loginError by remember { mutableStateOf<String?>(null) }
    var registerError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Loading -> isLoading = true
            is AuthState.Success -> {
                isLoading = false
                loginError = null
                registerError = null
                navController.navigate(Screen.Main.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                isLoading = false
                if (navController.currentDestination?.route == Screen.Login.route) {
                    loginError = state.message
                } else if (navController.currentDestination?.route == Screen.Register.route) {
                    registerError = state.message
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Idle -> isLoading = false
        }
    }

    val startDestination = if (isLoggedIn) Screen.Main.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onNavigateToLogin = { navController.navigate(Screen.Login.route) })
        }
        composable(Screen.Login.route) {
            LaunchedEffect(Unit) { loginError = null }
            LoginScreen(
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateBack = { navController.popBackStack() },
                errorMessage = loginError,
                isLoading = isLoading
            )
        }
        composable(Screen.Register.route) {
            LaunchedEffect(Unit) { registerError = null }
            RegisterScreen(
                onRegisterClick = { name, email, password -> authViewModel.register(name, email, password) },
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
                errorMessage = registerError,
                isLoading = isLoading
            )
        }

        composable(Screen.Main.route) { MainScreen(navController = navController) }

        composable(Screen.MissionPreferences.route) {
            MissionPreferencesScreen(
                onNavigateToGacha = { category, budget, distance ->
                    val route = "${Screen.Gacha.route}?category=$category&budget=$budget&distance=$distance"
                    navController.navigate(route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.Gacha.route}?category={category}&budget={budget}&distance={distance}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("budget") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("distance") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            val budget = backStackEntry.arguments?.getString("budget")
            val distance = backStackEntry.arguments?.getString("distance")

            GachaScreen(
                category = category,
                budget = budget,
                distance = distance,
                onMissionFound = { missionId ->
                    navController.navigate("${Screen.Guidance.route}/$missionId") {
                        popUpTo(Screen.Gacha.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.Guidance.route}/{missionId}",
            arguments = listOf(navArgument("missionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val missionId = backStackEntry.arguments?.getString("missionId")

            if (missionId == null) {
                navController.popBackStack()
            } else {
                GuidanceScreen(
                    missionId = missionId,
                    onGiveUpClick = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    // Kirim distance (Double) ke JournalEntry via route argument
                    onArrivedClick = { distance ->
                        // Konversi Double ke Float untuk passing via NavArg (lebih aman)
                        navController.navigate("${Screen.JournalEntry.route}?distance=${distance.toFloat()}")
                    }
                )
            }
        }

        // Screen.JournalEntry sekarang menerima argument optional distance
        composable(
            route = "${Screen.JournalEntry.route}?distance={distance}",
            arguments = listOf(
                navArgument("distance") { type = NavType.FloatType; defaultValue = 0f }
            )
        ) { backStackEntry ->
            val distance = backStackEntry.arguments?.getFloat("distance")?.toDouble() ?: 0.0

            JournalEntryScreen(
                realDistance = distance, // Pass distance ke screen
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
                    journalId = journalId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("${Screen.EditJournal.route}/$id") }
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

        composable(Screen.Notifications.route) { NotificationsCenterScreen(onBackClick = { navController.popBackStack() }) }
        composable(Screen.EditProfile.route) { EditProfileScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Settings.route) { SettingsScreen(onNavigateBack = { navController.popBackStack() }, authViewModel = authViewModel) }
        composable(Screen.DailyStreak.route) { DailyStreakScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.BadgeCollection.route) { BadgeCollectionScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.DetailedStats.route) { DetailedStatsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.AdventureSuggestion.route) { AdventureSuggestionScreen(onNavigateBack = { navController.popBackStack() }) }
    }
}