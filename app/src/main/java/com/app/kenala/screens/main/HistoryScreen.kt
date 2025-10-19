package com.app.kenala.screens.main // Pastikan ini ada di dalam package main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.AccentBlue
import com.app.kenala.ui.theme.LightBlue
import com.app.kenala.ui.theme.PrimaryBlue

// Data dummy untuk riwayat misi
private data class MissionHistory(
    val title: String,
    val date: String,
    val rating: Double,
    val imageUrl: String
)

private val historyList = listOf(
    MissionHistory(
        title = "Kedai Kopi 'Seroja'",
        date = "23 Sep 2025",
        rating = 4.0,
        imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=1887&auto=format&fit=crop"
    ),
    MissionHistory(
        title = "Taman Hutan Kota",
        date = "21 Sep 2025",
        rating = 5.0,
        imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=2070&auto=format&fit=crop"
    ),
    MissionHistory(
        title = "Galeri Seni Lokal",
        date = "15 Sep 2025",
        rating = 4.5,
        imageUrl = "https://images.unsplash.com/photo-1511895426328-8727b6205733?q=80&w=1887&auto=format&fit=crop"
    ),
    MissionHistory(
        title = "Mencicipi Teh Talua",
        date = "11 Sep 2025",
        rating = 5.0,
        imageUrl = "https://images.unsplash.com/photo-1593538465345-31381b45f492?q=80&w=1964&auto=format&fit=crop"
    )
)

/**
 * Layar History (Riwayat Misi)
 * Sesuai dengan Tampilan 9 di prototipe profesional.
 */
@Composable
fun HistoryScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(all = 25.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp) // Jarak antar kartu
        ) {
            // Judul Halaman
            item {
                Text(
                    text = "Riwayat Petualangan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Daftar Kartu Riwayat
            items(historyList) { mission ->
                HistoryCard(mission = mission)
            }
        }
    }
}

/**
 * Composable pribadi untuk satu kartu riwayat
 */
@Composable
private fun HistoryCard(mission: MissionHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // 16dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Putih
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gambar
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mission.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = mission.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(15.dp))

            // Judul & Tanggal
            Column(
                modifier = Modifier.weight(1f) // Ambil sisa ruang
            ) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = mission.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlue // Menggunakan warna dari palet baru
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Rating dengan ikon bintang
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = AccentBlue, // Warna bintang yang cerah
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${mission.rating}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
