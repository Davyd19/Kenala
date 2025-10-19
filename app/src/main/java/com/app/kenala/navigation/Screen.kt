package com.app.kenala.navigation

sealed class Screen(val route: String) {

    // --- Alur Awal (Autentikasi) ---
    object Onboarding : Screen("onboarding_screen")
    object Login : Screen("login_screen")
    // Tambahkan RegisterScreen di sini jika perlu
    // object Register : Screen("register_screen")

    // --- Alur Utama (Setelah Login) ---

    // Ini adalah "Host" atau "Rumah" untuk Bottom Navigation Bar
    object Main : Screen("main_screen")

    // Layar-layar di dalam Bottom Navigation Bar
    object Home : Screen("home_screen")
    object History : Screen("history_screen")
    object Profile : Screen("profile_screen")
    // Tambahkan ExploreScreen di sini jika Anda membuatnya
    // object Explore : Screen("explore_screen")

    // --- Alur Misi ---
    object Gacha : Screen("gacha_screen") // Layar loading pencarian misi
    object Guidance : Screen("guidance_screen") // Layar petunjuk misi
    object Arrival : Screen("arrival_screen") // Layar "Anda telah tiba"
    object Journal : Screen("journal_screen") // Layar tulis jurnal

    // --- Alur Profil ---
    object Settings : Screen("settings_screen")
    // Tambahkan EditProfile, dll. di sini
    // object EditProfile : Screen("edit_profile_screen")
}