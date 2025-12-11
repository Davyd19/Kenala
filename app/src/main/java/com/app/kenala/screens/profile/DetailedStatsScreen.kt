package com.app.kenala.screens.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel

private val allCategories = listOf(
    "Kuliner", "Alam", "Seni & Budaya", "Sejarah", "Rekreasi", "Belanja"
)

private fun getCategoryStyle(category: String): Pair<ImageVector, Color> {
    return when (category.lowercase()) {
        "kuliner" -> Icons.Default.Restaurant to AccentColor
        "seni & budaya" -> Icons.Default.Palette to OceanBlue
        "alam" -> Icons.Default.Park to ForestGreen
        "sejarah" -> Icons.Default.Museum to SkyBlue
        "rekreasi" -> Icons.Default.FitnessCenter to ErrorColor
        "belanja" -> Icons.Default.ShoppingBag to DeepBlue
        else -> Icons.Default.Explore to LightTextColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStats()
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

        if (isLoading && stats == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (stats == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Gagal memuat statistik.")
            }
        } else {
            DashboardContent(
                stats = stats!!,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun DashboardContent(stats: StatsDto, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            Text(
                text = "Ringkasan Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GradientStatCard( // Menggunakan GradientStatCard
                        label = "Misi Selesai",
                        value = stats.total_missions.toString(),
                        icon = Icons.Default.Flag,
                        gradient = listOf(ForestGreen, ForestGreen.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f)
                    )
                    GradientStatCard( // Menggunakan GradientStatCard
                        label = "Jarak Tempuh",
                        value = "${stats.total_distance.toInt()} km",
                        icon = Icons.Default.DirectionsRun,
                        gradient = listOf(OceanBlue, SkyBlue),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GradientStatCard( // Menggunakan GradientStatCard
                        label = "Hari Aktif",
                        value = stats.total_active_days.toString(),
                        icon = Icons.Default.CalendarToday,
                        gradient = listOf(SkyBlue, OceanBlue),
                        modifier = Modifier.weight(1f)
                    )
                    GradientStatCard( // Menggunakan GradientStatCard
                        label = "Jurnal Ditulis",
                        value = stats.journal_count.toString(),
                        icon = Icons.Default.Book,
                        gradient = listOf(AccentColor, AccentColor.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Progres Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            CategoryBarChart(breakdown = stats.category_breakdown)
        }
    }
}

// DEFINISI GradientStatCard BARU (Menggantikan StatCard Lama)
@Composable
private fun GradientStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp),
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


// FUNGSI StatCard LAMA TELAH DIHAPUS

@Composable
private fun CategoryBarChart(breakdown: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val categoryData = allCategories.map { categoryName ->
            val (icon, color) = getCategoryStyle(categoryName)
            val count = breakdown[categoryName] ?: 0
            Triple(categoryName, count, icon to color)
        }

        val maxTarget = (categoryData.maxOfOrNull { it.second } ?: 0).coerceAtLeast(5).toFloat()

        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categoryData.forEach { (name, count, style) ->
                BarChartItem(
                    label = name,
                    value = count,
                    target = maxTarget,
                    icon = style.first,
                    color = style.second
                )
            }
        }
    }
}

@Composable
private fun BarChartItem(
    label: String,
    value: Int,
    target: Float,
    icon: ImageVector,
    color: Color
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val progress = if (target > 0) (value / target).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "barProgress"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "$value misi",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}