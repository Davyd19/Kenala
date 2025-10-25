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
    object EditProfile : Screen("edit_profile_screen")
    object Settings : Screen("settings_screen")

    // --- Alur Misi ---
    object MissionPreferences : Screen("mission_preferences_screen")
    object Gacha : Screen("gacha_screen")
    object Guidance : Screen("guidance_screen")

    // --- Alur Jurnal ---
    object JournalEntry : Screen("journal_entry_screen")
    object JournalDetail : Screen("journal_detail_screen")
    object EditJournal : Screen("edit_journal_screen")

    // --- Alur Statistik ---
    object Statistics : Screen("statistics_screen")

    // --- Alur Notifikasi ---
    object Notifications : Screen("notifications_screen")
}

