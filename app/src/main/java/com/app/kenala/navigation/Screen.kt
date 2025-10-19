package com.app.kenala.navigation

sealed class Screen(val route: String) {

    // --- Alur Awal (Autentikasi) ---
    object Onboarding : Screen("onboarding_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")

    // --- Alur Utama (Setelah Login) ---
    object Main : Screen("main_screen")

    // Layar-layar di dalam Bottom Navigation Bar
    object Home : Screen("home_screen")
    object History : Screen("history_screen")
    object Profile : Screen("profile_screen")

    // --- Alur Misi ---
    object MissionPreferences : Screen("mission_preferences_screen")
    object Gacha : Screen("gacha_screen")
    object Guidance : Screen("guidance_screen")
    object Arrival : Screen("arrival_screen")
    object Journal : Screen("journal_screen")

    // --- Alur Profil ---
    object Settings : Screen("settings_screen")
}

