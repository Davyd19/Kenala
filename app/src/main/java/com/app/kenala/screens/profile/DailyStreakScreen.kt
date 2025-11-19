package com.app.kenala.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // <-- IMPORT BARU
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

private data class DayStreak(
    val date: LocalDate,
    val hasActivity: Boolean,
    val isToday: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStreakScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    // Ambil data dari ViewModel
    val streakData by viewModel.streakData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Ambil data saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchStreak()
    }

    // Gunakan data dari API, atau default 0 jika null
    val currentStreak = streakData?.currentStreak ?: 0
    val longestStreak = streakData?.longestStreak ?: 0
    val totalDays = streakData?.totalActiveDays ?: 0
    val activityMap = streakData?.recentActivity ?: emptyMap()

    // Generate 14 hari terakhir secara dinamis berdasarkan activityMap
    val last14Days = remember(activityMap) {
        val today = LocalDate.now()
        (13 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            // Format tanggal ke String "YYYY-MM-DD" untuk dicocokkan dengan Map
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            DayStreak(
                date = date,
                // Cek apakah tanggal ini ada di map activity dari backend
                hasActivity = activityMap.containsKey(dateString),
                isToday = daysAgo == 0
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Streak Petualangan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // --- PERBAIKAN 1: Gunakan AutoMirrored ---
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading && streakData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 25.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Current Streak Card
                item {
                    CurrentStreakCard(currentStreak = currentStreak)
                }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StreakStatCard(
                            label = "Terpanjang",
                            value = "$longestStreak hari",
                            icon = Icons.Default.EmojiEvents,
                            color = AccentColor,
                            modifier = Modifier.weight(1f)
                        )
                        StreakStatCard(
                            label = "Total Aktif",
                            value = "$totalDays hari",
                            icon = Icons.Default.CalendarMonth,
                            color = ForestGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Calendar View
                item {
                    Text(
                        text = "14 Hari Terakhir",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                        LazyRow(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(last14Days) { index, day ->
                                DayItem(day = day, position = index)
                            }
                        }
                    }
                }

                // Tips Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = AccentColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AccentColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tips Mempertahankan Streak",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Selesaikan minimal 1 misi setiap hari untuk mempertahankan streak-mu!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun CurrentStreakCard(currentStreak: Int) {
    var scale by remember { mutableFloatStateOf(0.8f) }

    LaunchedEffect(Unit) {
        scale = 1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
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
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "$currentStreak Hari",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Streak Saat Ini",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun StreakStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayItem(day: DayStreak, position: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(position * 30L)
        visible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.graphicsLayer { alpha = animatedAlpha }
    ) {
        Text(
            // --- PERBAIKAN 2: Gunakan Locale.forLanguageTag ---
            text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("id-ID")),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isToday && day.hasActivity -> AccentColor
                        day.hasActivity -> ForestGreen.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.hasActivity) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Aktif",
                    tint = if (day.isToday) DeepBlue else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}