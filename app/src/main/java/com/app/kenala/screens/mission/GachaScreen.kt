package com.app.kenala.screens.mission // Pastikan ini ada di dalam package mission

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.LocationCity
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.kenala.R
import com.app.kenala.ui.theme.AccentBlue
import com.app.kenala.ui.theme.BrightBlue
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.PrimaryDark
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

private enum class GachaState {
    Idle, Searching, Finished
}

/**
 * Layar Gacha (Pencarian Misi) - Versi "3D Card Flip"
 * Menggunakan graphicsLayer untuk animasi flip yang imersif.
 */
@Composable
fun GachaScreen(
    onMissionFound: () -> Unit // Navigasi ke layar petunjuk
) {
    var gachaState by remember { mutableStateOf(GachaState.Idle) }
    var revealedMission by remember { mutableStateOf<String?>(null) }
    var rotation by remember { mutableStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 400),
        label = "CardFlipAnimation"
    )

    // Memicu animasi saat gachaState berubah
    LaunchedEffect(gachaState) {
        if (gachaState == GachaState.Searching) {
            val totalSearchTime = 4000L // Total 4 detik
            val startTime = System.currentTimeMillis()
            var currentDelay = 100L

            while (System.currentTimeMillis() - startTime < totalSearchTime) {
                rotation += 180f
                delay(currentDelay)
                // Perlambat putaran seiring waktu
                if (System.currentTimeMillis() - startTime > 2500L) {
                    currentDelay = 300L
                }
            }
            // Pilih misi pemenang dan masuk ke state Finished
            revealedMission = dummyMissions.random()
            gachaState = GachaState.Finished
            // Pastikan kartu berhenti menghadap ke depan
            if (rotation % 360 != 0f) {
                rotation += 180f
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent // Atur transparan agar background Box terlihat
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient( // Latar belakang radial yang dramatis
                        colors = listOf(PrimaryBlue, PrimaryDark),
                        radius = 800f
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // 1. Teks Judul
                Text(
                    text = when (gachaState) {
                        GachaState.Idle -> "Tarik Kartu Petualanganmu"
                        GachaState.Searching -> "Mencari Misi..."
                        GachaState.Finished -> "Misi Ditemukan!"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BrightBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(60.dp))

                // 2. Kartu Misi dengan Animasi Flip
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationY = animatedRotation
                            cameraDistance = 8 * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // --- PERBAIKAN LOGIKA TAMPILAN ---
                    // Jika state Idle, selalu tampilkan bagian belakang.
                    // Jika tidak, gunakan logika rotasi untuk menentukan sisi mana yang terlihat.
                    if (gachaState == GachaState.Idle) {
                        MissionCardBack()
                    } else {
                        if (animatedRotation.toInt() % 360 < 90 || animatedRotation.toInt() % 360 > 270) {
                            MissionCardFront(missionName = revealedMission ?: "")
                        } else {
                            MissionCardBack()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                // 3. Tombol Aksi
                Button(
                    onClick = {
                        when (gachaState) {
                            GachaState.Idle -> gachaState = GachaState.Searching
                            GachaState.Finished -> onMissionFound()
                            else -> {} // Jangan lakukan apa-apa saat sedang mencari
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 40.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrightBlue,
                        contentColor = PrimaryDark,
                        disabledContainerColor = BrightBlue.copy(alpha = 0.5f)
                    ),
                    enabled = gachaState != GachaState.Searching
                ) {
                    Text(
                        text = if (gachaState == GachaState.Finished) "LIHAT MISI" else "TARIK KARTU",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** Composable untuk bagian depan kartu (menampilkan misi) */
@Composable
private fun MissionCardFront(missionName: String) {
    Card(
        modifier = Modifier.size(width = 180.dp, height = 250.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationCity,
                contentDescription = "Misi",
                modifier = Modifier.size(60.dp),
                tint = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = missionName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Composable untuk bagian belakang kartu (kartu misteri dengan logo) */
@Composable
private fun MissionCardBack() {
    Card(
        modifier = Modifier
            .size(width = 180.dp, height = 250.dp)
            .graphicsLayer { rotationY = 180f } // Balik kartu ini
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(colors = listOf(AccentBlue, BrightBlue)),
                shape = MaterialTheme.shapes.large
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PrimaryDark.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kenala),
                contentDescription = "Logo Kenala",
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            )
        }
    }
}

