package com.app.kenala.screens.main // Pastikan ini ada di dalam package main

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

/**
 * Layar Utama yang menampung Bottom Navigation Bar dan NavHost
 * untuk layar-layar utama (Home, History, Profile).
 */
@Composable
fun MainScreen(navController: NavHostController) {
    // 1. Buat NavController BARU khusus untuk navigasi bawah
    val mainNavController = rememberNavController()

    Scaffold(
        // 2. Tampilkan navigasi bawah
        bottomBar = {
            KenalaBottomNav(navController = mainNavController)
        }
    ) { innerPadding ->
        // 3. Tampilkan "panggung" untuk layar-layar utama
        //    di dalam padding yang diberikan oleh Scaffold
        MainNavGraph(
            mainNavController = mainNavController,
            appNavController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Composable pribadi yang mendefinisikan tampilan Bottom Navigation Bar.
 */
@Composable
private fun KenalaBottomNav(navController: NavHostController) {
    // Daftar item-item navigasi
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface, // Putih (CardBackgroundColor)
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant // Abu-abu (LightTextColor)
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
                        // Selalu kembali ke Home jika tombol back ditekan
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Hindari duplikasi layar
                        launchSingleTop = true
                        // Pulihkan state saat kembali
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface // Hapus indikator
                )
            )
        }
    }
}

/**
 * Composable pribadi yang mendefinisikan NavGraph untuk layar-layar
 * yang ada di dalam MainScreen.
 */
@Composable
private fun MainNavGraph(mainNavController: NavHostController, appNavController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = mainNavController,
        startDestination = Screen.Home.route, // Mulai dari Home
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = appNavController)
        }
        composable(Screen.History.route) {
            HistoryScreen() // INI AKAN ERROR (NORMAL)
        }
        composable(Screen.Profile.route) {
            ProfileScreen() // INI AKAN ERROR (NORMAL)
        }
    }
}

/**
 * Data class pribadi untuk merepresentasikan item di navigasi bawah.
 */
private sealed class BottomNavItem(
    val title: String,
    val route: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    object Home : BottomNavItem(
        title = "Home",
        route = Screen.Home.route,
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home
    )
    object History : BottomNavItem(
        title = "Riwayat",
        route = Screen.History.route,
        filledIcon = Icons.Filled.Book,
        outlinedIcon = Icons.Outlined.Book
    )
    object Profile : BottomNavItem(
        title = "Profil",
        route = Screen.Profile.route,
        filledIcon = Icons.Filled.Person,
        outlinedIcon = Icons.Outlined.Person
    )
}