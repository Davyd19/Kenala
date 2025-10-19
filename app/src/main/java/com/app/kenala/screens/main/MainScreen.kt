package com.app.kenala.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.kenala.navigation.Screen

@Composable
fun MainScreen(navController: NavHostController) {
    val mainNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            KenalaBottomNav(navController = mainNavController)
        }
    ) { innerPadding ->
        MainNavGraph(
            mainNavController = mainNavController,
            appNavController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun KenalaBottomNav(navController: NavHostController) {
    // ... (kode tidak berubah) ...
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    val icon = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                        screen.filledIcon
                    } else {
                        screen.outlinedIcon
                    }
                    Icon(icon, contentDescription = screen.title)
                },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
private fun MainNavGraph(mainNavController: NavHostController, appNavController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = mainNavController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = appNavController,
                onNavigateToNotifications = { appNavController.navigate(Screen.Notifications.route) }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(onJournalClick = { journalId ->
                appNavController.navigate("${Screen.JournalDetail.route}/$journalId")
            })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToStats = {
                    appNavController.navigate(Screen.Statistics.route)
                },
                onNavigateToEditProfile = {
                    appNavController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSettings = {
                    appNavController.navigate(Screen.Settings.route)
            }
            )
        }
    }
}

private sealed class BottomNavItem(
    val title: String,
    val route: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    object Home : BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home)
    object History : BottomNavItem("Riwayat", Screen.History.route, Icons.Filled.Book, Icons.Outlined.Book)
    object Profile : BottomNavItem("Profil", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
}

