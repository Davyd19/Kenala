package com.app.kenala.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.kenala.navigation.Screen
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel

@Composable
fun MainScreen(navController: NavHostController, ) {
    val mainNavController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()

    Scaffold(
        bottomBar = {
            KenalaBottomNav(navController = mainNavController)
        }
    ) { innerPadding ->
        MainNavGraph(
            mainNavController = mainNavController,
            appNavController = navController,
            profileViewModel = profileViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun KenalaBottomNav(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == screen.route
                } == true

                BottomNavItem(
                    icon = if (selected) screen.filledIcon else screen.outlinedIcon,
                    label = screen.title,
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(56.dp),
        color = if (selected) {
            AccentColor.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(26.dp)
                    .scale(scale),
                tint = if (selected) AccentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentColor)
                )
            }
        }
    }
}

@Composable
private fun MainNavGraph(
    mainNavController: NavHostController,
    appNavController: NavHostController,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = mainNavController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = appNavController,
                profileViewModel = profileViewModel,
                onNavigateToNotifications = {
                    appNavController.navigate(Screen.Notifications.route)
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onJournalClick = { journalId ->
                    appNavController.navigate("${Screen.JournalDetail.route}/$journalId")
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onNavigateToEditProfile = {
                    appNavController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSettings = {
                    appNavController.navigate(Screen.Settings.route)
                },
                onNavigateToStreak = {
                    appNavController.navigate(Screen.DailyStreak.route)
                },
                onNavigateToBadges = {
                    appNavController.navigate(Screen.BadgeCollection.route)
                },
                onNavigateToDetailedStats = {
                    appNavController.navigate(Screen.DetailedStats.route)
                },
                onNavigateToSuggestions = {
                    appNavController.navigate(Screen.AdventureSuggestion.route)
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
    object Home : BottomNavItem(
        "Home",
        Screen.Home.route,
        Icons.Filled.Home,
        Icons.Outlined.Home
    )
    object History : BottomNavItem(
        "Riwayat",
        Screen.History.route,
        Icons.Filled.Book,
        Icons.Outlined.Book
    )
    object Profile : BottomNavItem(
        "Profil",
        Screen.Profile.route,
        Icons.Filled.Person,
        Icons.Outlined.Person
    )
}