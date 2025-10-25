package com.app.kenala.screens.mission

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.kenala.R
import com.app.kenala.ui.theme.*
import kotlinx.coroutines.delay

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
    var rotation by remember { mutableFloatStateOf(0f) }
    var shimmerAlpha by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "CardFlip"
    )

    val shimmerAnimation by animateFloatAsState(
        targetValue = shimmerAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Shimmer"
    )

    LaunchedEffect(gachaState) {
        if (gachaState == GachaState.Searching) {
            shimmerAlpha = 1f
            val totalSearchTime = 3500L
            val startTime = System.currentTimeMillis()
            var flipDelay = 150L

            while (System.currentTimeMillis() - startTime < totalSearchTime) {
                rotation += 180f
                delay(flipDelay)

                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed > totalSearchTime * 0.6f) {
                    flipDelay = 400L
                }
            }

            revealedMission = dummyMissions.random()
            gachaState = GachaState.Finished
            shimmerAlpha = 0f

            if (rotation.toInt() % 360 != 0) {
                rotation += 180f
            }
        }
    }

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
                // Animated sparkles background
                if (gachaState == GachaState.Searching) {
                    SparkleEffect(modifier = Modifier.size(300.dp))
                }

                Text(
                    text = when (gachaState) {
                        GachaState.Idle -> "Tarik Kartu Petualanganmu"
                        GachaState.Searching -> "Mencari Misi Sempurna..."
                        GachaState.Finished -> "🎉 Misi Ditemukan!"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (gachaState == GachaState.Finished) AccentColor else Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                if (gachaState == GachaState.Searching) {
                    Text(
                        text = "Sedang menganalisis preferensimu...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, horizontal = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            rotationY = animatedRotation
                            cameraDistance = 12f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (gachaState == GachaState.Idle) {
                        MissionCardBack()
                    } else {
                        val normalizedRotation = animatedRotation % 360
                        if (normalizedRotation < 90 || normalizedRotation > 270) {
                            MissionCardFront(missionName = revealedMission ?: "")
                        } else {
                            MissionCardBack()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

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
                        disabledContainerColor = AccentColor.copy(alpha = 0.4f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
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
            .graphicsLayer { rotationY = 180f }
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
                painter = painterResource(id = R.drawable.logo_kenala),
                contentDescription = "Logo Kenala",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { rotationY = 180f }
            )
        }
    }
}

@Composable
private fun SparkleEffect(modifier: Modifier = Modifier) {
    var rotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            rotation += 2f
            delay(16)
        }
    }

    Box(
        modifier = modifier
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        repeat(8) { index ->
            val angle = (360f / 8f) * index
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(x = 100.dp)
                    .rotate(angle)
                    .background(AccentColor.copy(alpha = 0.6f), MaterialTheme.shapes.small)
            )
        }
    }
}