package com.app.kenala.screens.mission // Pastikan ini ada di dalam package mission

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith // Ganti 'with' dengan 'togetherWith'
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity // Menggunakan ikon yang kita tahu ada
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.kenala.ui.theme.BorderColor
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryDark
import com.app.kenala.ui.theme.SecondaryColor
import kotlinx.coroutines.delay

// Daftar data dummy untuk kartu-kartu misi
private val dummyMissions = listOf(
    "Kedai Kopi Seroja",
    "Taman Hutan Kota",
    "Galeri Seni Lokal",
    "Pasar Raya Padang",
    "Museum Adityawarman",
    "Pantai Air Manis",
    "Jembatan Siti Nurbaya"
)

/**
 * Layar Gacha (Pencarian Misi) - Versi "Card Shuffle" (V5)
 * Menggunakan AnimatedContent untuk efek acak kartu.
 */
@Composable
fun GachaScreen(
    onMissionFound: () -> Unit // Navigasi ke layar petunjuk
) {
    var isSearching by remember { mutableStateOf(false) }
    // Kartu "target" yang akan ditampilkan. Kita mulai dengan "?"
    var currentMission by remember { mutableStateOf("?") }

    // Memicu animasi saat 'isSearching' berubah menjadi true
    LaunchedEffect(isSearching) {
        if (isSearching) {
            val startTime = System.currentTimeMillis()

            // 1. Fase Mengacak Cepat (3 detik)
            while (System.currentTimeMillis() - startTime < 3000) {
                currentMission = dummyMissions.random()
                delay(100L) // Ganti kartu setiap 100ms
            }

            // 2. Fase Mengacak Lambat (2 detik)
            while (System.currentTimeMillis() - startTime < 5000) {
                currentMission = dummyMissions.random()
                delay(400L) // Ganti kartu lebih lambat untuk suspense
            }

            // 3. Tentukan Pemenang
            currentMission = dummyMissions.random()

            // 4. Jeda untuk melihat pemenang
            delay(2500)

            // 5. Pindah ke layar berikutnya
            onMissionFound()
        }
    }

    Scaffold(
        containerColor = PrimaryDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 1. Teks Judul
            Text(
                text = if (isSearching) "Mengacak Kartu..." else "Tarik Kartu Petualanganmu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(60.dp))

            // 2. Kontainer untuk Kartu (dengan efek fade di tepi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .fadingEdge(PrimaryDark), // Efek fade
                contentAlignment = Alignment.Center
            ) {
                // AnimatedContent adalah inti dari "gacha" ini
                AnimatedContent(
                    targetState = currentMission,
                    label = "Gacha Card Shuffle",
                    transitionSpec = {
                        // Kartu baru masuk dari kanan, kartu lama keluar ke kiri
                        (slideInHorizontally { fullWidth -> fullWidth } + fadeIn())
                            .togetherWith(slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut())
                    }
                ) { missionName ->
                    // Tampilkan kartu pemenang
                    LocationCard(missionName = missionName)
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // 3. Tombol Aksi (hanya terlihat jika belum mencari)
            if (!isSearching) {
                Button(
                    onClick = { isSearching = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryColor,
                        contentColor = PrimaryDark
                    )
                ) {
                    Text(
                        text = "TARIK KARTU",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Composable pribadi untuk Kartu Misi
 */
@Composable
private fun LocationCard(missionName: String) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 220.dp) // Ukuran seperti kartu remi
            .border(2.dp, BorderColor, MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large, // 16dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tampilkan ikon "?" jika itu kartu awal
            val isStartingCard = (missionName == "?")

            Icon(
                imageVector = if (isStartingCard) Icons.Default.QuestionMark else Icons.Default.LocationCity,
                contentDescription = "Misi",
                modifier = Modifier.size(60.dp),
                tint = if (isStartingCard) LightTextColor else PrimaryDark
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = missionName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isStartingCard) LightTextColor else PrimaryDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Modifier kustom untuk menambah efek gradien "fade" di tepi kiri dan kanan
 * (Versi ini lebih baik dari yang di GachaScreen sebelumnya)
 */
private fun Modifier.fadingEdge(backgroundColor: Color): Modifier = this
    .graphicsLayer(alpha = 0.99f) // Trik untuk mengaktifkan blending
    .drawWithCache {
        val fadeBrush = Brush.horizontalGradient(
            0.0f to backgroundColor,
            0.2f to Color.Transparent, // Mulai transparan di 20%
            0.8f to Color.Transparent, // Selesai transparan di 80%
            1.0f to backgroundColor,
        )
        onDrawWithContent {
            drawContent()
            drawRect(
                brush = fadeBrush,
                blendMode = BlendMode.DstIn
            )
        }
    }