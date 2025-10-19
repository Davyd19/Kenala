package com.app.kenala.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.navigation.Screen
import com.app.kenala.ui.theme.AccentBlue
import com.app.kenala.ui.theme.BrightBlue
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.PrimaryDark

// --- Data Dummy Baru untuk Fitur Home Screen ---
private data class InspirationCategory(val title: String, val imageUrl: String)
private val inspirations = listOf(
    InspirationCategory("Kuliner Tersembunyi", "https://i.ibb.co/6PpHx5t/placeholder-cafe.jpg"),
    InspirationCategory("Seni & Budaya", "https://i.ibb.co/JqDBLbf/placeholder-art.jpg"),
    InspirationCategory("Alam Kota", "https://i.ibb.co/L5hY5M2/placeholder-nature.jpg"),
    InspirationCategory("Belanja Unik", "https://i.ibb.co/kH0C3bV/placeholder-bookstore.jpg")
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 25.dp)
        ) {
            item { HomeHeader(onNavigateToNotifications = onNavigateToNotifications) }
            item { MainMissionCard(navController = navController) }
            // --- STATISTIK DIKEMBALIKAN KE SINI ---
            item { StatsHighlightCard() }
            item { WeeklyChallengeCard() }
            item { AdventureInspirationSection() }
        }
    }
}

@Composable
private fun HomeHeader(onNavigateToNotifications: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 25.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hai, Nayla!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Level 5: Petualang Lokal",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextColor
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi"
                )
            }
            AsyncImage(
                model = "https://i.pravatar.cc/100?u=nayla",
                contentDescription = "Avatar Profil",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun MainMissionCard(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = listOf(PrimaryBlue, AccentBlue)))
                .padding(25.dp)
        ) {
            Column {
                Text(
                    text = "Siap untuk Misi Baru?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrightBlue
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { navController.navigate(Screen.MissionPreferences.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = "CARI MISI SEKARANG",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsHighlightCard() {
    Column(modifier = Modifier.padding(start = 25.dp, end = 25.dp, top = 25.dp)) {
        Text(
            text = "Ringkasan Petualangan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 15.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatItem(label = "Misi Selesai", value = "12", modifier = Modifier.weight(1f))
            StatItem(label = "Jarak Tempuh", value = "42 km", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = LightTextColor)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WeeklyChallengeCard() {
    Column(modifier = Modifier.padding(start = 25.dp, end = 25.dp, top = 25.dp)) {
        Text(
            text = "Tantangan Mingguan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 15.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = PrimaryDark)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Tantangan",
                    tint = AccentBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Jelajah Kuliner",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Selesaikan 2 misi kuliner minggu ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrightBlue.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdventureInspirationSection() {
    Column(modifier = Modifier.padding(top = 25.dp)) {
        Text(
            text = "Inspirasi Petualangan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 25.dp, bottom = 15.dp)
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
        modifier = Modifier.width(160.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.height(200.dp),
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
            Box( // Gradien untuk keterbacaan teks
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

