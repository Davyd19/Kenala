package com.app.kenala.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.kenala.data.local.entities.UserEntity
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.screens.profile.AchievementPreviewCard // <-- 1. IMPORT SUDAH DITAMBAHKAN
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(), // Terima ViewModel
    onNavigateToStats: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStreak: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToDetailedStats: () -> Unit,
    onNavigateToSuggestions: () -> Unit
) {
    // Ambil data dari ViewModel
    val user by profileViewModel.user.collectAsState()
    val stats by profileViewModel.stats.collectAsState()

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
            item { ProfileHeader(user = user, stats = stats) } // Pass data
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Quick Stats Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStatCard(
                        value = stats?.total_missions?.toString() ?: "0", // Data dinamis
                        label = "Misi Selesai",
                        icon = Icons.Default.Flag,
                        gradient = listOf(ForestGreen, ForestGreen.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        value = "${stats?.total_distance?.toInt() ?: 0} km", // Data dinamis
                        label = "Jarak Tempuh",
                        icon = Icons.Default.DirectionsRun,
                        gradient = listOf(OceanBlue, SkyBlue),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Streak Card
            item {
                StreakCard(
                    currentStreak = stats?.current_streak ?: 0, // Data dinamis
                    longestStreak = stats?.longest_streak ?: 0, // Data dinamis
                    totalActiveDays = stats?.total_active_days ?: 0, // Data dinamis
                    onClick = onNavigateToStreak
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Achievements Preview
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pencapaian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToBadges) {
                        Text("Lihat Semua", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                // <-- 2. PANGGILAN INI SEKARANG MENGGUNAKAN IMPORT DI ATAS
                AchievementPreviewCard(onClick = onNavigateToBadges)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Menu Section
            item {
                Text(
                    text = "Lainnya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 8.dp)
                )
            }
            item {
                MenuCard {
                    MenuItem(
                        title = "Statistik Detail",
                        subtitle = "Lihat progres lengkap",
                        icon = Icons.Filled.Analytics,
                        onClick = onNavigateToDetailedStats
                    )
                    HorizontalDivider(
                        color = BorderColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    MenuItem(
                        title = "Saran Lokasi",
                        subtitle = "Usulkan tempat petualangan",
                        icon = Icons.Filled.AddLocation,
                        onClick = onNavigateToSuggestions
                    )
                    HorizontalDivider(
                        color = BorderColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
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
private fun ProfileHeader(user: UserEntity?, stats: StatsDto?) {
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
            // Gunakan AsyncImage jika ada URL, jika tidak, gunakan inisial
            if (!user?.profile_image_url.isNullOrEmpty()) {
                AsyncImage(
                    model = user?.profile_image_url,
                    contentDescription = "Avatar Profil",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = user?.name?.firstOrNull()?.toString()?.uppercase() ?: "K", // Data dinamis
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = user?.name ?: "Pengguna Kenala", // Data dinamis
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Level ${stats?.level ?: 1} • Petualang Lokal", // Data dinamis
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier
) {
    Card(
        modifier = modifier // Hapus modifier ukuran tetap
            .height(100.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = gradient)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    totalActiveDays: Int,
    onClick: () -> Unit
) {
    var rotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 0f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            ) { value, _ ->
                rotation = value
            }
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GradientStart,
                            GradientEnd
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    AccentColor.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AccentColor,
                                modifier = Modifier
                                    .size(32.dp)
                                    .rotate(rotation)
                            )
                        }
                        Column {
                            Text(
                                text = "Streak Petualangan",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "$currentStreak hari berturut-turut",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Lihat Detail",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "$longestStreak hari",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Terpanjang",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = AccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "$totalActiveDays hari",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Total Aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// <-- 3. FUNGSI ACHIEVEMENTPREVIEWCARD PALSU TELAH DIHAPUS DARI SINI

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