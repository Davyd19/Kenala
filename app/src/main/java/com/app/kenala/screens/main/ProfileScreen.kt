package com.app.kenala.screens.main // Pastikan ini ada di dalam package main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.kenala.ui.theme.AccentBlue
import com.app.kenala.ui.theme.BorderColor
import com.app.kenala.ui.theme.LightBlue
import com.app.kenala.ui.theme.PrimaryBlue

/**
 * Layar Profile
 * Sesuai dengan Tampilan 8 di prototipe profesional.
 */
@Composable
fun ProfileScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Judul Halaman
            Text(
                text = "Profil & Pencapaian",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 25.dp, top = 20.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Header Profil (Avatar & Nama)
            ProfileHeader()

            Spacer(modifier = Modifier.height(30.dp))

            // Daftar Menu
            MenuCard()
        }
    }
}

/**
 * Composable pribadi untuk header profil (Avatar, Nama)
 */
@Composable
private fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar dengan gradien baru
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryBlue, AccentBlue) // Gradien yang konsisten
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                style = MaterialTheme.typography.displayMedium, // 45px
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = "Nayla Nurul Afifah",
            style = MaterialTheme.typography.titleLarge, // 22px
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Composable pribadi untuk kartu yang berisi menu
 */
@Composable
private fun MenuCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        shape = MaterialTheme.shapes.large, // 16dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Putih
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            MenuItem(
                title = "Edit Profil",
                icon = Icons.Filled.Edit,
                onClick = { /* TODO: Navigasi ke EditProfileScreen */ }
            )
            Divider(color = BorderColor, modifier = Modifier.padding(horizontal = 20.dp))
            MenuItem(
                title = "Papan Peringkat",
                icon = Icons.Filled.Leaderboard,
                onClick = { /* TODO: Navigasi ke LeaderboardScreen */ }
            )
            Divider(color = BorderColor, modifier = Modifier.padding(horizontal = 20.dp))
            MenuItem(
                title = "Pengaturan Akun",
                icon = Icons.Filled.Settings,
                onClick = { /* TODO: Navigasi ke SettingsScreen */ }
            )
        }
    }
}

/**
 * Composable pribadi untuk satu baris item menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card( // Kita gunakan Card agar bisa merespon klik
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall, // 0dp
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryBlue, // Warna ikon baru yang lebih menarik
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge, // 16px
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Panah",
                tint = LightBlue, // Warna panah yang lebih lembut
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
