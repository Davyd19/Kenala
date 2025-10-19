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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryColor
import com.app.kenala.ui.theme.PrimaryDark
import com.app.kenala.ui.theme.SecondaryColor
import androidx.navigation.NavHostController
import com.app.kenala.navigation.Screen


/**
 * Layar Home
 * Sesuai dengan Tampilan 3 di prototipe profesional.
 */
@Composable
fun HomeScreen(navController: NavHostController) {
    // Scaffold memberi kita background yang sudah di-tema
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        // LazyColumn digunakan agar layar bisa di-scroll jika kontennya panjang
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Item 1: Header (Nama & Avatar)
            item { HomeHeader() }

            // Item 2: Kartu Misi Utama
            item { MainMissionCard(navController = navController) }

            // Item 3: Judul Section "Misi Pilihan"
            item {
                Text(
                    text = "Misi Pilihan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 25.dp, top = 25.dp, bottom = 15.dp)
                )
            }

            // Item 4: Daftar Misi Pilihan (Horizontal)
            item { MissionChoiceRow() }

            // Spacer di akhir
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
                style = MaterialTheme.typography.headlineSmall, // 24px
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Level 5: Petualang Lokal",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextColor
            )
        }
        // Avatar (dari prototipe)
        AsyncImage(
            model = "https://i.pravatar.cc/100?u=nayla", // URL Avatar dari prototipe
            contentDescription = "Avatar Profil",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SecondaryColor)
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
        shape = MaterialTheme.shapes.extraLarge, // 24dp
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        // Gradien Biru Gelap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryDark, PrimaryColor)
                    )
                )
                .padding(25.dp)
        ) {
            Column {
                Text(
                    text = "Siap untuk Misi Baru?",
                    style = MaterialTheme.typography.titleLarge, // 22px
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // TODO: Tambahkan preference grid di sini nanti
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { navController.navigate(Screen.Gacha.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large, // 16dp
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryDark
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
 * Composable pribadi untuk daftar Misi Pilihan (scroll horizontal)
 */
@Composable
private fun MissionChoiceRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 25.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp) // Jarak antar kartu
    ) {
        item {
            MissionChoiceCard(
                imageUrl = "https://i.ibb.co/JqDBLbf/placeholder-art.jpg",
                title = "Jelajahi Pameran Seni"
            )
        }
        item {
            MissionChoiceCard(
                imageUrl = "https://i.ibb.co/mHjkfJ6/placeholder-tea.jpg",
                title = "Cicipi Teh Talua Otentik"
            )
        }
        item {
            MissionChoiceCard(
                imageUrl = "https://i.ibb.co/kH0C3bV/placeholder-bookstore.jpg",
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
        modifier = Modifier.width(160.dp), // Lebar kartu
        shape = MaterialTheme.shapes.large, // 16dp
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Gambar
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
            // Judul
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}