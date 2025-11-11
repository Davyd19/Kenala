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
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.navigation.Screen
import com.app.kenala.ui.theme.*
import com.app.kenala.viewmodel.ProfileViewModel

private data class InspirationCategory(val title: String, val imageUrl: String)
private val inspirations = listOf(
    InspirationCategory("Kuliner Tersembunyi", "https://images.pexels.com/photos/1267696/pexels-photo-1267696.jpeg"),
    InspirationCategory("Seni & Budaya", "https://images.pexels.com/photos/269923/pexels-photo-269923.jpeg"),
    InspirationCategory("Alam Kota", "https://images.pexels.com/photos/459225/pexels-photo-459225.jpeg"),
    InspirationCategory("Belanja Unik", "https://images.pexels.com/photos/1106468/pexels-photo-1106468.jpeg")
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(), // Terima ViewModel
    onNavigateToNotifications: () -> Unit
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
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 25.dp)
        ) {
            item {
                HomeHeader(
                    userName = user?.name,
                    userLevel = stats?.level,
                    avatarUrl = user?.profile_image_url,
                    onNavigateToNotifications = onNavigateToNotifications
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { MainMissionCard(navController = navController) }
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { StatsHighlightCard(stats = stats) } // Pass stats
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { WeeklyChallengeCard() } // Biarkan statis dulu
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { AdventureInspirationSection() }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String?,
    userLevel: Int?,
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
                text = "Hai, ${userName ?: "Petualang"}!", // Gunakan data dinamis
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
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
                    text = "Level ${userLevel ?: 1}: Petualang Lokal", // Gunakan data dinamis
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
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
            AsyncImage(
                model = avatarUrl ?: "https://i.pravatar.cc/100", // Gunakan avatarUrl
                contentDescription = "Avatar Profil",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Fallback background
            )
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
private fun StatsHighlightCard(stats: StatsDto?) { // Terima stats
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
                value = stats?.total_missions?.toString() ?: "0", // Data dinamis
                color = ForestGreen,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Jarak Tempuh",
                value = "${stats?.total_distance?.toInt() ?: 0} km", // Data dinamis
                color = SkyBlue,
                modifier = Modifier.weight(1f)
            )
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
private fun WeeklyChallengeCard() {
    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
        Text(
            text = "Tantangan Mingguan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(AccentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Tantangan",
                        tint = AccentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Jelajah Kuliner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selesaikan 2 misi kuliner minggu ini",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.5f },
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentColor,
                        trackColor = AccentColor.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdventureInspirationSection() {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = "Inspirasi Petualangan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 25.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 25.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(inspirations) { inspiration ->
                InspirationCard(item = inspiration)
            }
        }
    }
}

@Composable
private fun InspirationCard(item: InspirationCategory) {
    Card(
        modifier = Modifier.width(180.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.height(240.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 200f
                        )
                    )
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}