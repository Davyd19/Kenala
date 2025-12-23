package com.app.kenala.navigation

sealed class Screen(val route: String) {

    object Splash : Screen("splash_screen")
    object Onboarding : Screen("onboarding_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")


    object Main : Screen("main_screen")

    object Home : Screen("home_screen")
    object History : Screen("history_screen")
    object Profile : Screen("profile_screen")
    object EditProfile : Screen("edit_profile_screen")
    object Settings : Screen("settings_screen")

    object MissionPreferences : Screen("mission_preferences_screen")
    object Gacha : Screen("gacha_screen")
    object Guidance : Screen("guidance_screen")

    object JournalEntry : Screen("journal_entry_screen")
    object JournalDetail : Screen("journal_detail_screen")
    object EditJournal : Screen("edit_journal_screen")

    object Statistics : Screen("statistics_screen")

    object Notifications : Screen("notifications_screen")

    object DailyStreak : Screen("daily_streak_screen")
    object BadgeCollection : Screen("badge_collection_screen")
    object DetailedStats : Screen("detailed_stats_screen")
    object AdventureSuggestion : Screen("adventure_suggestion_screen")
}