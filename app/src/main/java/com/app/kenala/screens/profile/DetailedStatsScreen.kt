package com.app.kenala.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.kenala.ui.theme.*

private data class StatCategory(
    val name: String,
    val items: List<StatItem>
)

private data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

private data class Achievement(
    val name: String,
    val progress: Float,
    val current: Int,
    val target: Int,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedStatsScreen(onNavigateBack: () -> Unit) {
    val stats = remember {
        listOf(
            StatCategory(
                "Aktivitas",
                listOf(
                    StatItem("Total Misi", "12", Icons.Default.Flag, ForestGreen),
                    StatItem("Misi Minggu Ini", "3", Icons.Default.CalendarToday, OceanBlue),
                    StatItem("Jarak Tempuh", "42 km", Icons.Default.DirectionsRun, AccentColor),
                    StatItem("Rata-rata/Misi", "3.5 km", Icons.Default.Timeline, SkyBlue)
                )
            ),
            StatCategory(
                "Kategori Favorit",
                listOf(
                    StatItem("Kuliner", "5 misi", Icons.Default.Restaurant, AccentColor),
                    StatItem("Seni & Budaya", "4 misi", Icons.Default.Palette, OceanBlue),
                    StatItem("Alam", "2 misi", Icons.Default.Park, ForestGreen),
                    StatItem("Sejarah", "1 misi", Icons.Default.Museum, SkyBlue)
                )
            ),
            StatCategory(
                "Prestasi Waktu",
                listOf(
                    StatItem("Hari Aktif", "45 hari", Icons.Default.Event, ForestGreen),
                    StatItem("Streak Terpanjang", "15 hari", Icons.Default.LocalFireDepartment, ErrorColor),
                    StatItem("Waktu Favorit", "Sore Hari", Icons.Default.WbSunny, AccentColor),
                    StatItem("Bergabung Sejak", "Sep 2025", Icons.Default.CalendarMonth, OceanBlue)
                )
            )
        )
    }

    val achievements = remember {
        listOf(
            Achievement("Misi Selesai", 0.24f, 12, 50, Icons.Default.Flag, ForestGreen),
            Achievement("Jarak Tempuh", 0.42f, 42, 100, Icons.Default.DirectionsRun, OceanBlue),
            Achievement("Foto Upload", 0.65f, 13, 20, Icons.Default.CameraAlt, AccentColor),
            Achievement("Teman Diajak", 0.0f, 0, 5, Icons.Default.Group, SkyBlue)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistik Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 25.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Overall Summary Card
            item {
                OverallSummaryCard()
            }

            // Progress Section
            item {
                Text(
                    text = "Progres Pencapaian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(achievements) { achievement ->
                AchievementProgressCard(achievement)
            }

            // Stats Categories
            stats.forEach { category ->
                item {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column {
                            category.items.forEachIndexed { index, item ->
                                StatItemRow(item)
                                if (index < category.items.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = BorderColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryItem("12", "Misi Selesai", Icons.Default.Flag)
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    SummaryItem("42 km", "Jarak Tempuh", Icons.Default.DirectionsRun)
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    SummaryItem("Level 5", "Petualang", Icons.Default.Star)
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AchievementProgressCard(achievement: Achievement) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        animatedProgress = achievement.progress
    }

    val progress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = achievement.color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                achievement.color.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = achievement.icon,
                            contentDescription = null,
                            tint = achievement.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = achievement.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${achievement.current} / ${achievement.target}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${(achievement.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = achievement.color
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = achievement.color,
                trackColor = achievement.color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun StatItemRow(item: StatItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = item.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = item.color
        )
    }
}