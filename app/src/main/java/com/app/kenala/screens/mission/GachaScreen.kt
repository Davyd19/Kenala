package com.app.kenala.screens.mission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.kenala.R
import com.app.kenala.ui.theme.*


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

@Composable
fun GachaScreen(onMissionFound: () -> Unit) {
    var gachaState by remember { mutableStateOf(GachaState.Idle) }
    var revealedMission by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DeepBlue.copy(alpha = 0.9f),
                            MidnightBlue
                        ),
                        radius = 1200f
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val (title, subtitle) = when (gachaState) {
                    GachaState.Idle -> "Tarik Kartu Petualanganmu" to ""
                    GachaState.Searching -> "Mencari Misi Sempurna..." to "Menganalisis preferensimu..."
                    GachaState.Finished -> "🎉 Misi Ditemukan!" to (revealedMission ?: "")
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (gachaState == GachaState.Finished) AccentColor else Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                if (gachaState == GachaState.Searching || gachaState == GachaState.Finished) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier.size(width = 200.dp, height = 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (gachaState) {
                        GachaState.Idle -> {
                            // Tampilkan kartu belakang statis
                            MissionCardBack()
                        }
                        GachaState.Searching -> {
                            // Putar video
                            VideoGachaPlayer( // Pastikan file VideoGachaPlayer.kt ada di paket yang sama
                                modifier = Modifier.fillMaxSize(),
                                onVideoEnded = {
                                    revealedMission = dummyMissions.random()
                                    gachaState = GachaState.Finished
                                }
                            )
                        }
                        GachaState.Finished -> {
                            // Tampilkan kartu depan dengan transisi mulus
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(500)) +
                                        scaleIn(animationSpec = tween(500))
                            ) {
                                MissionCardFront(missionName = revealedMission ?: "")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Tombol ini akan muncul kembali saat Finished
                AnimatedVisibility(visible = gachaState != GachaState.Searching) {
                    Button(
                        onClick = {
                            when (gachaState) {
                                GachaState.Idle -> gachaState = GachaState.Searching
                                GachaState.Finished -> onMissionFound()
                                else -> {}
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 40.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = DeepBlue,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp
                        )
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
}

@Composable
private fun MissionCardFront(missionName: String) {
    Card(
        modifier = Modifier.size(width = 200.dp, height = 280.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            AccentColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            AccentColor.copy(alpha = 0.1f),
                            MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = "Misi",
                        modifier = Modifier.size(48.dp),
                        tint = AccentColor
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = missionName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.titleLarge.lineHeight
                )
            }
        }
    }
}

@Composable
private fun MissionCardBack() {
    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 280.dp)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(AccentColor, AccentColor.copy(alpha = 0.5f))
                ),
                shape = MaterialTheme.shapes.extraLarge
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = DeepBlue.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            OceanBlue.copy(alpha = 0.6f),
                            DeepBlue.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kenala), // Pastikan logo_kenala ada di res/drawable
                contentDescription = "Logo Kenala",
                modifier = Modifier.size(120.dp)
            )
        }
    }
}