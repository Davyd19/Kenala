package com.app.kenala.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.kenala.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStreak: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToDetailedStats: () -> Unit,
    onNavigateToSuggestions: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = "Profil & Pencapaian",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 25.dp, top = 20.dp)
                )
            }
            item { ProfileHeader() }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Activity Section
            item {
                Text(
                    text = "Aktivitas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 8.dp)
                )
            }
            item {
                MenuCard {
                    MenuItem(
                        title = "Streak Harian",
                        subtitle = "7 hari berturut-turut",
                        icon = Icons.Filled.LocalFireDepartment,
                        onClick = onNavigateToStreak
                    )
                    HorizontalDivider(
                        color = BorderColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    MenuItem(
                        title = "Koleksi Badge",
                        subtitle = "4 dari 10 badge terbuka",
                        icon = Icons.Filled.EmojiEvents,
                        onClick = onNavigateToBadges
                    )
                    HorizontalDivider(
                        color = BorderColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    MenuItem(
                        title = "Statistik Detail",
                        subtitle = "Lihat progres lengkap",
                        icon = Icons.Filled.Analytics,
                        onClick = onNavigateToDetailedStats
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Contribution Section
            item {
                Text(
                    text = "Kontribusi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 8.dp)
                )
            }
            item {
                MenuCard {
                    MenuItem(
                        title = "Saran Lokasi",
                        subtitle = "Usulkan tempat petualangan",
                        icon = Icons.Filled.AddLocation,
                        onClick = onNavigateToSuggestions
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Account Section
            item {
                Text(
                    text = "Akun",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 8.dp)
                )
            }
            item {
                MenuCard {
                    MenuItem(
                        title = "Edit Profil",
                        subtitle = "Ubah informasi pribadi",
                        icon = Icons.Filled.Edit,
                        onClick = onNavigateToEditProfile
                    )
                    HorizontalDivider(
                        color = BorderColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    MenuItem(
                        title = "Pengaturan Akun",
                        subtitle = "Preferensi & keamanan",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToSettings
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(OceanBlue, DeepBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Nayla Nurul Afifah",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Level 5 • Petualang Lokal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MenuCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Panah",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}