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
    Searching, // Animasi shuffle & API call berjalan
    Revealing, // Animasi reveal kartu hasil
    ShowingInfo, // Dialog muncul
    Finished // Menunggu navigasi
}

@Composable
fun GachaScreen(
    // PERUBAHAN: Menerima missionId, bukan () -> Unit
    onMissionFound: (missionId: String) -> Unit,
    category: String? = null,
    budget: String? = null,
    distance: String? = null,
    missionViewModel: MissionViewModel = viewModel()
) {
    var gachaState by remember { mutableStateOf(GachaState.Idle) }
    var missionDistance by remember { mutableStateOf<String?>(null) }
    var estimatedTime by remember { mutableIntStateOf(0) }
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

    // --- !!! PERBAIKAN BUG DI SINI !!! ---
    LaunchedEffect(selectedMission, gachaState) {
        if (selectedMission != null && gachaState == GachaState.Revealing) {
            if (locationManager.hasLocationPermission()) {
                locationManager.getCurrentLocation { location ->
                    // Mengganti .let{} dengan if/else untuk menangani 'location' null
                    if (location != null) {
                        // KASUS SUKSES: Lokasi didapat
                        val distanceMeters = LocationManager.calculateDistance(
                            location.latitude,
                            location.longitude,
                            selectedMission!!.latitude,
                            selectedMission!!.longitude
                        )
                        missionDistance = LocationManager.formatDistance(distanceMeters)
                        estimatedTime = LocationManager.estimateTime(distanceMeters)
                        gachaState = GachaState.ShowingInfo
                    } else {
                        // KASUS GAGAL: Lokasi null (GPS mati, dll)
                        missionDistance = "Tidak diketahui"
                        estimatedTime = 0
                        gachaState = GachaState.ShowingInfo
                    }
                }
            } else {
                // KASUS GAGAL: Tidak ada izin lokasi
                missionDistance = "Tidak diketahui"
                estimatedTime = 0
                gachaState = GachaState.ShowingInfo
            }
        }
    }
    // --- AKHIR PERBAIKAN ---

    LaunchedEffect(isAnimatingSearch, isLoading, selectedMission, error) {
        if (gachaState == GachaState.Searching && !isAnimatingSearch && !isLoading) {
            if (selectedMission != null) {
                gachaState = GachaState.Revealing
            } else {
                gachaState = GachaState.Idle
            }
        }
    }

    // PERUBAHAN: LaunchedEffect ini sekarang MENGIRIM ID MISI
    LaunchedEffect(gachaState) {
        if (gachaState == GachaState.Finished) {
            // Pastikan selectedMission tidak null sebelum mengirim ID
            selectedMission?.id?.let {
                onMissionFound(it) // Mengirim ID misi ke NavGraph
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
                        colors = listOf(
                            DeepBlue.copy(alpha = 0.9f),
                            MidnightBlue
                        ),
                        radius = 1200f
                    )
                )
                .padding(innerPadding)
        ) {
            EnhancedFloatingParticles(isActive = gachaState == GachaState.Searching)
            AmbientParticles()

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val (title, subtitle, instruction) = when (gachaState) {
                    GachaState.Idle -> Triple("Tarik Kartu Petualanganmu", "", "Ketuk kartu untuk memulai")
                    GachaState.ReadyToPull -> Triple("Siap Untuk Petualangan?", "", "Tarik kartu ke bawah!")
                    GachaState.Searching -> Triple("Mencari Misi Sempurna...", "Menganalisis preferensimu...", "")
                    GachaState.Revealing -> Triple("Misi Ditemukan!", "", "")
                    GachaState.ShowingInfo, GachaState.Finished -> Triple(
                        "🎉 Misi Ditemukan!",
                        selectedMission?.name ?: "",
                        ""
                    )
                }

                AnimatedText(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (gachaState == GachaState.Finished || gachaState == GachaState.ShowingInfo || gachaState == GachaState.Revealing)
                        AccentColor else Color.White
                )

                if (subtitle.isNotEmpty() && gachaState != GachaState.ShowingInfo && gachaState != GachaState.Finished) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                if (instruction.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PulsingText(
                        text = instruction,
                        color = AccentColor
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier.size(width = 200.dp, height = 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (gachaState) {
                        GachaState.Idle -> {
                            InteractiveMissionCardBack(
                                onClick = { gachaState = GachaState.ReadyToPull }
                            )
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
                                        animate(dragOffset, 0f, animationSpec = spring()) { value, _ ->
                                            dragOffset = value
                                        }
                                    }
                                }
                            )
                        }
                        GachaState.Searching -> {
                            if (isAnimatingSearch) {
                                EpicShufflingCards(
                                    onAnimationComplete = {
                                        isAnimatingSearch = false
                                    }
                                )
                            }
                            if (!isAnimatingSearch && isLoading) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                        GachaState.Revealing, GachaState.ShowingInfo, GachaState.Finished -> {
                            selectedMission?.let { mission ->
                                EpicRevealCard(missionName = mission.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }

            if (gachaState == GachaState.ShowingInfo && selectedMission != null && missionDistance != null) {
                MissionInfoDialog(
                    missionName = selectedMission!!.name,
                    distance = missionDistance!!,
                    estimatedTime = estimatedTime,
                    onDismissRequest = { // Saat klik di luar dialog
                        // PERUBAHAN: Sekarang aman untuk membersihkan misi
                        gachaState = GachaState.Idle
                        missionViewModel.clearSelectedMission()
                        missionDistance = null
                    },
                    onDismissButton = { // Saat klik tombol "Cari Misi Lain"
                        gachaState = GachaState.Idle
                        missionViewModel.clearSelectedMission()
                        missionDistance = null
                    },
                    onAccept = { // Saat klik tombol "Mulai Petualangan"
                        gachaState = GachaState.Finished
                    }
                )
            }

            error?.let { errorMsg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { missionViewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(errorMsg)
                }
            }
        }
    }
}

@Composable
private fun AnimatedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "textAlpha"
    )

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
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .alpha(alpha)
    )
}

@Composable
private fun PulsingText(text: String, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color.copy(alpha = alpha),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun AmbientParticles() {
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

    particles.forEach { particle ->
        AnimatedParticle(particle, isIntense = false)
    }
}

@Composable
private fun EnhancedFloatingParticles(isActive: Boolean) {
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
        particles.forEach { particle ->
            AnimatedParticle(particle, isIntense = true)
        }
    }
}

private data class ParticleData(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val duration: Int,
    val delay: Int
)

@Composable
private fun AnimatedParticle(particle: ParticleData, isIntense: Boolean) {
    var offsetY by remember { mutableFloatStateOf(particle.startY) }
    var offsetX by remember { mutableFloatStateOf(particle.startX) }
    var alpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(particle.delay.toLong())

        while (true) {
            animate(0f, 0.8f, animationSpec = tween(300)) { value, _ ->
                alpha = value
            }

            animate(
                initialValue = particle.startY,
                targetValue = -200f,
                animationSpec = tween(particle.duration, easing = LinearEasing)
            ) { value, _ ->
                offsetY = value
                offsetX = particle.startX + kotlin.math.sin(value / 50f) * 20f
            }

            animate(0.8f, 0f, animationSpec = tween(300)) { value, _ ->
                alpha = value
            }

            offsetY = particle.startY
            offsetX = particle.startX

            if (!isIntense) delay(Random.nextLong(1000, 3000))
        }
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(particle.size.dp)
            .alpha(alpha)
            .blur(1.dp)
            .background(
                if (isIntense) AccentColor else Color.White.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small
            )
    )
}

@Composable
private fun InteractiveMissionCardBack(onClick: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var glowAnimation by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            animate(0.3f, 1f, animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )) { value, _ ->
                glowAnimation = value
            }
        }
    }

    Card(
        onClick = {
            coroutineScope.launch {
                animate(1f, 0.9f, animationSpec = tween(100)) { value, _ -> scale = value }
                animate(0.9f, 1f, animationSpec = spring()) { value, _ -> scale = value }
            }
            onClick()
        },
        modifier = Modifier
            .size(width = 200.dp, height = 280.dp)
            .scale(scale)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AccentColor.copy(alpha = glowAnimation),
                        AccentColor.copy(alpha = 0.5f)
                    )
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

@Composable
private fun DraggableMissionCard(
    dragOffset: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val rotation = (dragOffset / 10f).coerceIn(-15f, 15f)
    val scale = 1f + (dragOffset / 500f).coerceIn(0f, 0.15f)
    val glowIntensity = (dragOffset / 150f).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 280.dp)
            .offset(y = dragOffset.dp)
            .scale(scale)
            .rotate(rotation)
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AccentColor.copy(alpha = glowIntensity),
                        AccentColor.copy(alpha = 0.5f)
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (dragOffset + dragAmount.y).coerceIn(0f, 200f)
                        onDrag(newOffset)
                    }
                )
            },
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

@Composable
private fun EpicShufflingCards(onAnimationComplete: () -> Unit) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var alpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        repeat(3) { cycle ->
            animate(0f, 720f, animationSpec = tween(800, easing = FastOutSlowInEasing)) { value, _ ->
                rotation = value
            }

            animate(1f, 1.3f, animationSpec = tween(200)) { value, _ ->
                scale = value
            }
            animate(1.3f, 0.8f, animationSpec = tween(200)) { value, _ ->
                scale = value
            }

            val angle = cycle * 120f
            animate(0f, 60f, animationSpec = tween(400)) { value, _ ->
                offsetX = kotlin.math.cos(Math.toRadians((angle + value).toDouble())).toFloat() * 50f
                offsetY = kotlin.math.sin(Math.toRadians((angle + value).toDouble())).toFloat() * 50f
            }
        }

        animate(offsetX, 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { value, _ ->
            offsetX = value
        }
        animate(offsetY, 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { value, _ ->
            offsetY = value
        }

        repeat(5) {
            animate(rotation, rotation + 180f, animationSpec = tween(150)) { value, _ ->
                rotation = value
            }
            animate(1f, 1.15f, animationSpec = tween(75)) { value, _ ->
                scale = value
            }
            animate(1.15f, 1f, animationSpec = tween(75)) { value, _ ->
                scale = value
            }
            delay(50)
        }

        animate(1f, 0f, animationSpec = tween(300)) { value, _ ->
            alpha = value
        }

        delay(100)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .scale(scale)
            .rotate(rotation)
            .alpha(alpha)
    ) {
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
                    painter = painterResource(id = R.drawable.logo_kenala),
                    contentDescription = "Logo Kenala",
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}

@Composable
private fun EpicRevealCard(missionName: String) {
    var scale by remember { mutableFloatStateOf(0f) }
    var rotation by remember { mutableFloatStateOf(180f) }
    var alpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        animate(0f, 1.2f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )) { value, _ ->
            scale = value
        }

        animate(180f, 360f, animationSpec = tween(600, easing = FastOutSlowInEasing)) { value, _ ->
            rotation = value
        }

        animate(0f, 1f, animationSpec = tween(600)) { value, _ ->
            alpha = value
        }

        animate(1.2f, 1f, animationSpec = spring()) { value, _ ->
            scale = value
        }
    }

    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 280.dp)
            .scale(scale)
            .graphicsLayer {
                rotationY = rotation
            }
            .alpha(alpha),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            AccentColor.copy(alpha = 0.08f)
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
                            AccentColor.copy(alpha = 0.15f),
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