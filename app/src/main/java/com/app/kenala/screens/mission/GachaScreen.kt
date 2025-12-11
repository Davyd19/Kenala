package com.app.kenala.screens.mission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.R
import com.app.kenala.ui.theme.*
import com.app.kenala.utils.LocationManager
import com.app.kenala.viewmodel.MissionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class GachaState {
    Idle,
    ReadyToPull,
    Searching,
    Revealing,
    ShowingInfo,
    Finished
}

@Composable
fun GachaScreen(
    onMissionFound: (missionId: String) -> Unit,
    category: String? = null,
    budget: String? = null,
    distance: String? = null,
    missionViewModel: MissionViewModel = viewModel()
) {
    var gachaState by remember { mutableStateOf(GachaState.Idle) }
    var missionDistance by remember { mutableStateOf<String?>(null) }
    var estimatedTime by remember { mutableStateOf<Int?>(null) }

    // Kita gunakan mutableFloatStateOf untuk dragOffset agar performa drag responsif (synchronous)
    // Untuk animasi balik (fling), kita gunakan animate() dengan qualifier lengkap untuk hindari error
    var dragOffset by remember { mutableFloatStateOf(0f) }

    var isAnimatingSearch by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val selectedMission by missionViewModel.selectedMission.collectAsState()
    val isLoading by missionViewModel.isLoading.collectAsState()
    val error by missionViewModel.error.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(selectedMission, gachaState) {
        if (selectedMission != null && gachaState == GachaState.Revealing) {
            missionDistance = null
            estimatedTime = null
            if (locationManager.hasLocationPermission()) {
                locationManager.getCurrentLocation { location ->
                    if (location != null) {
                        val distanceMeters = LocationManager.calculateDistance(
                            location.latitude,
                            location.longitude,
                            selectedMission!!.latitude,
                            selectedMission!!.longitude
                        )
                        missionDistance = LocationManager.formatDistance(distanceMeters)
                        estimatedTime = LocationManager.estimateTime(distanceMeters)
                        // Trigger pindah ke dialog info setelah data siap (atau timeout visual di EpicRevealCard)
                    } else {
                        missionDistance = "Tidak diketahui"
                        estimatedTime = 0
                    }
                }
            } else {
                missionDistance = "Izin Ditolak"
                estimatedTime = 0
            }
        }
    }

    LaunchedEffect(isAnimatingSearch, isLoading, selectedMission, error) {
        if (gachaState == GachaState.Searching && !isAnimatingSearch && !isLoading) {
            if (selectedMission != null) {
                gachaState = GachaState.Revealing
            } else {
                gachaState = GachaState.Idle
            }
        }
    }

    LaunchedEffect(gachaState) {
        if (gachaState == GachaState.Finished) {
            selectedMission?.id?.let {
                onMissionFound(it)
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            gachaState = GachaState.Idle
            isAnimatingSearch = false
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
                        colors = listOf(DeepBlue.copy(alpha = 0.9f), MidnightBlue),
                        radius = 1200f
                    )
                )
                .padding(innerPadding)
        ) {
            AmbientParticles()
            EnhancedFloatingParticles(isActive = gachaState == GachaState.Searching)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val (title, subtitle) = when (gachaState) {
                    GachaState.Idle -> "Tarik Kartu Petualangan" to "Ketuk kartu untuk memulai"
                    GachaState.ReadyToPull -> "Siap Berpetualang?" to "Tarik ke bawah!"
                    GachaState.Searching -> "Mencari Misi..." to "Menyesuaikan preferensimu"
                    GachaState.Revealing -> "Misi Ditemukan!" to ""
                    GachaState.ShowingInfo, GachaState.Finished -> " " to ""
                }

                if (gachaState != GachaState.ShowingInfo) {
                    AnimatedText(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (gachaState == GachaState.Revealing) AccentColor else Color.White
                    )
                    if (subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier.size(width = 220.dp, height = 320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (gachaState) {
                        GachaState.Idle -> {
                            InteractiveMissionCardBack(onClick = { gachaState = GachaState.ReadyToPull })
                        }
                        GachaState.ReadyToPull -> {
                            DraggableMissionCard(
                                dragOffset = dragOffset,
                                onDragStart = {},
                                onDrag = { offset ->
                                    dragOffset = offset
                                    if (dragOffset > 150f) {
                                        gachaState = GachaState.Searching
                                        isAnimatingSearch = true
                                        dragOffset = 0f
                                        missionViewModel.getRandomMission(category, budget, distance)
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        // PENTING: Gunakan fully qualified name untuk menghindari ambiguitas
                                        androidx.compose.animation.core.animate(
                                            initialValue = dragOffset,
                                            targetValue = 0f,
                                            animationSpec = spring()
                                        ) { value, _ -> dragOffset = value }
                                    }
                                }
                            )
                        }
                        GachaState.Searching -> {
                            if (isAnimatingSearch) {
                                EpicShufflingCards(
                                    onAnimationComplete = { isAnimatingSearch = false }
                                )
                            } else if (isLoading) {
                                CircularProgressIndicator(color = AccentColor)
                            }
                        }
                        GachaState.Revealing -> {
                            selectedMission?.let { mission ->
                                EpicRevealCard(
                                    missionName = mission.name,
                                    onRevealFinished = {
                                        gachaState = GachaState.ShowingInfo
                                    }
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }

            if (gachaState == GachaState.ShowingInfo && selectedMission != null) {
                MissionInfoDialog(
                    missionName = selectedMission!!.name,
                    distance = missionDistance,
                    estimatedTime = estimatedTime,
                    onDismissRequest = {
                        gachaState = GachaState.Idle
                        missionViewModel.clearSelectedMission()
                    },
                    onDismissButton = {
                        gachaState = GachaState.Idle
                        missionViewModel.clearSelectedMission()
                    },
                    onAccept = {
                        gachaState = GachaState.Finished
                    }
                )
            }

            error?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = { missionViewModel.clearError() }) { Text("OK") } },
                    containerColor = ErrorColor
                ) { Text(it, color = Color.White) }
            }
        }
    }
}

// --- KOMPONEN UI & ANIMASI (FIXED WITH Animatable) ---

@Composable
private fun AnimatedText(text: String, style: androidx.compose.ui.text.TextStyle, color: Color) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(500), label = "textAlpha")
    LaunchedEffect(text) {
        visible = false
        delay(50)
        visible = true
    }
    Text(
        text = text,
        style = style,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(alpha)
    )
}

@Composable
private fun InteractiveMissionCardBack(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    // Menggunakan Animatable untuk scale agar tidak konflik
    val scale = remember { Animatable(1f) }

    // Efek klik sederhana
    LaunchedEffect(scale) {
        // Reset scale jika perlu
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxSize()
            .scale(scale.value)
            .border(3.dp, Brush.verticalGradient(listOf(AccentColor.copy(alpha = glowAlpha), Color.Transparent)), MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = DeepBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_kenala),
                contentDescription = null,
                modifier = Modifier.size(100.dp).alpha(0.8f)
            )
        }
    }
}

@Composable
private fun DraggableMissionCard(
    dragOffset: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val rotation = (dragOffset / 10f).coerceIn(-10f, 10f)
    val scale = 1f - (dragOffset / 1000f).coerceIn(0f, 0.1f)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = dragOffset.dp)
            .rotate(rotation)
            .scale(scale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (dragOffset + dragAmount.y).coerceAtLeast(0f)
                        onDrag(newOffset)
                    }
                )
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = DeepBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_kenala),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Icon(Icons.Default.KeyboardArrowDown, null, tint = AccentColor, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp))
        }
    }
}

@Composable
private fun EpicShufflingCards(onAnimationComplete: () -> Unit) {
    // Pengganti mutableFloatStateOf + animate() yang error
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Animasi Shuffle Cycle
        repeat(3) { cycle ->
            // Rotasi berputar cepat
            launch {
                rotation.animateTo(
                    targetValue = (cycle + 1) * 360f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )
            }

            // Scale membesar-mengecil (pulse)
            launch {
                scale.animateTo(1.3f, tween(200))
                scale.animateTo(0.8f, tween(200))
            }

            // Gerakan orbit (circular)
            launch {
                val angle = cycle * 120f
                val rad = Math.toRadians(angle.toDouble())
                offsetX.snapTo(kotlin.math.cos(rad).toFloat() * 50f)
                offsetY.snapTo(kotlin.math.sin(rad).toFloat() * 50f)

                // Kembali ke tengah
                offsetX.animateTo(0f, tween(400))
                offsetY.animateTo(0f, tween(400))
            }

            delay(800) // Tunggu 1 cycle selesai
        }

        // Final Shake
        repeat(5) {
            rotation.animateTo(rotation.value + 10f, tween(50))
            rotation.animateTo(rotation.value - 10f, tween(50))
        }
        rotation.animateTo(0f, spring())

        // Fade Out
        alpha.animateTo(0f, tween(300))

        delay(100)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX.value.dp, y = offsetY.value.dp)
            .scale(scale.value)
            .rotate(rotation.value)
            .alpha(alpha.value)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
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
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}

@Composable
private fun EpicRevealCard(missionName: String, onRevealFinished: () -> Unit) {
    // Gunakan Animatable untuk animasi yang bersih
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(180f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        // Paralel animations
        launch {
            scale.animateTo(
                1.2f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            scale.animateTo(1f, spring())
        }

        launch {
            rotation.animateTo(360f, tween(600, easing = FastOutSlowInEasing))
        }

        launch {
            alpha.animateTo(1f, tween(600))
        }

        delay(1000) // Tahan sebentar agar user bisa baca nama misi
        onRevealFinished()
    }

    // Tentukan sisi mana yang terlihat
    val isFrontVisible = rotation.value >= 270f || rotation.value <= 90f

    Card(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale.value)
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12f * density
            }
            .alpha(alpha.value),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        if (rotation.value > 270f) { // Sisi Depan (Hasil)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White, AccentColor.copy(alpha = 0.08f))
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
                            .background(AccentColor.copy(alpha = 0.15f), MaterialTheme.shapes.large),
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
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else { // Sisi Belakang (Cover)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlue)
            )
        }
    }
}

// --- PARTICLE EFFECTS (FIXED) ---

private data class ParticleData(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val duration: Int,
    val delay: Int
)

@Composable
fun AmbientParticles() {
    val particles = remember {
        (1..25).map {
            ParticleData(
                startX = Random.nextFloat() * 400f - 200f,
                startY = Random.nextFloat() * 800f,
                size = Random.nextFloat() * 4f + 2f,
                duration = Random.nextInt(3000, 5000),
                delay = Random.nextInt(0, 2000)
            )
        }
    }
    particles.forEach { particle -> AnimatedParticle(particle, isIntense = false) }
}

@Composable
fun EnhancedFloatingParticles(isActive: Boolean) {
    val particles = remember {
        (1..50).map {
            ParticleData(
                startX = Random.nextFloat() * 400f - 200f,
                startY = Random.nextFloat() * 200f + 400f,
                size = Random.nextFloat() * 6f + 3f,
                duration = Random.nextInt(1500, 3000),
                delay = Random.nextInt(0, 1000)
            )
        }
    }
    if (isActive) {
        particles.forEach { particle -> AnimatedParticle(particle, isIntense = true) }
    }
}

@Composable
private fun AnimatedParticle(particle: ParticleData, isIntense: Boolean) {
    // Gunakan Animatable
    val offsetY = remember { Animatable(particle.startY) }
    val offsetX = remember { Animatable(particle.startX) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(particle.delay.toLong())

        while (true) {
            // Reset
            offsetY.snapTo(particle.startY)
            offsetX.snapTo(particle.startX)
            alpha.snapTo(0f)

            // Fade In
            launch { alpha.animateTo(0.8f, tween(300)) }

            // Move Up
            launch {
                offsetY.animateTo(-200f, tween(particle.duration, easing = LinearEasing))
            }

            // Wiggle X (Manual Simulation using loop)
            launch {
                val startTime = withFrameNanos { it }
                while (offsetY.value > -200f) {
                    val time = (withFrameNanos { it } - startTime) / 1_000_000f // ms
                    val wave = kotlin.math.sin(time / 500f) * 20f
                    offsetX.snapTo(particle.startX + wave)
                }
            }

            // Tunggu gerakan selesai (agak kasar tapi cukup untuk partikel)
            delay(particle.duration - 300L)

            // Fade Out
            alpha.animateTo(0f, tween(300))

            // Random delay before rebirth
            if (!isIntense) delay(Random.nextLong(1000, 3000))
        }
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX.value.dp, y = offsetY.value.dp)
            .size(particle.size.dp)
            .alpha(alpha.value)
            .blur(1.dp)
            .background(
                if (isIntense) AccentColor else Color.White.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small
            )
    )
}