package com.app.kenala.screens.main // Pastikan ini ada di dalam package main

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.navigation.Screen
import com.app.kenala.ui.theme.AccentBlue
import com.app.kenala.ui.theme.BrightBlue
import com.app.kenala.ui.theme.LightBlue
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.PrimaryDark


/**
 * Layar Home
 * Sesuai dengan Tampilan 3 di prototipe profesional.
 */
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { HomeHeader() }
            item { MainMissionCard(navController = navController) }

            // --- FITUR BARU: STATISTIK & MISI TERAKHIR ---
            item { StatsSummaryCard() }
            item { LastMissionCard() }

            item {
                Text(
                    text = "Misi Pilihan Untukmu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 25.dp, top = 25.dp, bottom = 15.dp)
                )
            }
            item { MissionChoiceRow() }
            item { Spacer(modifier = Modifier.height(25.dp)) }
        }
    }
}

/**
 * Composable pribadi untuk Header (Hai, Nayla! & Avatar)
 */
@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 25.dp, end = 25.dp, top = 20.dp, bottom = 10.dp),
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
                color = LightBlue
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

/**
 * Composable pribadi untuk Kartu Misi Utama
 */
@Composable private fun MainMissionCard(navController: NavHostController) {
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
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryBlue, AccentBlue)
                    )
                )
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

/**
 * --- BARU: Kartu untuk ringkasan statistik pengguna ---
 */
@Composable
private fun StatsSummaryCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        StatItem(
            icon = Icons.Default.CardGiftcard,
            value = "12",
            label = "Misi Selesai",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            icon = Icons.Default.Route,
            value = "8.4 km",
            label = "Jarak Tempuh",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = LightBlue)
            }
        }
    }
}

/**
 * --- BARU: Kartu untuk menampilkan misi terakhir yang diselesaikan ---
 */
@Composable
private fun LastMissionCard() {
    Column(modifier = Modifier.padding(horizontal = 25.dp, vertical = 10.dp)) {
        Text(
            text = "Misi Terakhir Kamu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1593538465345-31381b45f492?q=80&w=1964&auto=format&fit=crop",
                    contentDescription = "Misi Terakhir",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Cicipi Teh Talua Otentik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "2 hari yang lalu", style = MaterialTheme.typography.bodyMedium, color = LightBlue)
                }
            }
        }
    }
}

/**
 * Composable pribadi untuk daftar Misi Pilihan (scroll horizontal)
 */
@Composable
private fun MissionChoiceRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 25.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            MissionChoiceCard(
                imageUrl = "https://images.unsplash.com/photo-1511895426328-8727b6205733?q=80&w=1887&auto=format&fit=crop",
                title = "Jelajahi Pameran Seni"
            )
        }
        item {
            MissionChoiceCard(
                imageUrl = "https://images.unsplash.com/photo-1550399105-c4db5fb85c18?q=80&w=2071&auto=format&fit=crop",
                title = "Temukan Buku Langka"
            )
        }
    }
}

/**
 * Composable pribadi untuk satu kartu Misi Pilihan
 */
@Composable
private fun MissionChoiceCard(imageUrl: String, title: String) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

