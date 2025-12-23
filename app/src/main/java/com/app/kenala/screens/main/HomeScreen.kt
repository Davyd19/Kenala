package com.app.kenala.screens.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.navigation.Screen
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel


@Composable
fun HomeScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(),
    onNavigateToNotifications: () -> Unit
) {
    val user by profileViewModel.user.collectAsState()
    val stats by profileViewModel.stats.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.refresh()
    }

    val dailyTip = remember {
        listOf(
            "Selalu siapkan air minum sebelum memulai perjalanan.",
            "Gunakan sepatu yang nyaman untuk eksplorasi jarak jauh.",
            "Jangan lupa mengisi baterai HP hingga penuh!",
            "Menyapa warga lokal bisa membuka petualangan baru.",
            "Ambil foto, tapi jangan lupa nikmati momennya.",
            "Cek ramalan cuaca sebelum berangkat mencari misi.",
            "Petualangan terbaik seringkali terjadi tanpa rencana."
        ).random()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 25.dp)
        ) {
            item {
                HomeHeader(
                    userName = user?.name,
                    avatarUrl = user?.profile_image_url,
                    onNavigateToNotifications = onNavigateToNotifications
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { MainMissionCard(navController = navController) }
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { StatsHighlightCard(stats = stats) }
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { DailyTipsSection(tip = dailyTip) }
            item { Spacer(modifier = Modifier.height(20.dp)) }

        }
    }
}

@Composable
private fun HomeHeader(
    userName: String?,
    avatarUrl: String?,
    onNavigateToNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 25.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hai, ${userName ?: "Petualang"}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OceanBlue, DeepBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = userName?.firstOrNull()?.toString()?.uppercase() ?: "P",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MainMissionCard(navController: NavHostController) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .scale(scale),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
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
                .padding(28.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Misi Baru Menantimu",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AccentColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Jelajahi Tempat\nTersembunyi di Kota",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        navController.navigate(Screen.MissionPreferences.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = DeepBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "MULAI PETUALANGAN",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsHighlightCard(stats: StatsDto?) {
    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
        Text(
            text = "Pencapaianmu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItem(
                label = "Misi Selesai",
                value = stats?.total_missions?.toString() ?: "0",
                color = ForestGreen,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Jarak Tempuh",
                value = "${stats?.total_distance?.toInt() ?: 0} km",
                color = SkyBlue,
                modifier = Modifier.weight(1f)
            )
        }

        if (stats?.category_breakdown?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kategori Favorit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            stats.category_breakdown.entries.take(3).forEach { (category, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icon = when (category.lowercase()) {
                            "kuliner" -> Icons.Default.Restaurant
                            "seni & budaya" -> Icons.Default.Palette
                            "alam" -> Icons.Default.Park
                            "sejarah" -> Icons.Default.Museum
                            "rekreasi" -> Icons.Default.FitnessCenter
                            else -> Icons.Default.Explore
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "$count misi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DailyTipsSection(tip: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Tips Hari Ini",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = tip,
                    fontSize = 14.sp,
                    color = Color(0xFF0D47A1),
                    lineHeight = 20.sp
                )
            }
        }
    }
}