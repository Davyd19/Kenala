package com.app.kenala.screens.mission

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.unit.sp
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
    Finished,
    NotFound
}

@Composable
fun GachaScreen(
    onMissionFound: (missionId: String) -> Unit,
    onNavigateToPreferences: () -> Unit,
    category: String? = null,
    budget: String? = null,
    distance: String? = null,
    missionViewModel: MissionViewModel = viewModel()
) {
    var gachaState by remember { mutableStateOf(GachaState.Idle) }
    var missionDistance by remember { mutableStateOf<String?>(null) }
    var estimatedTime by remember { mutableStateOf<Int?>(null) }
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

    // Handle Error State (Not Found)
    LaunchedEffect(error) {
        if (error != null) {
            if (error!!.contains("Tidak ada misi", ignoreCase = true) || error!!.contains("Misi Tidak Ditemukan", ignoreCase = true)) {
                // Delay sedikit agar animasi searching sempat terlihat/selesai cycle-nya jika terlalu cepat
                if (isAnimatingSearch) {
                    delay(500)
                }
                gachaState = GachaState.NotFound
                isAnimatingSearch = false
            } else {
                isAnimatingSearch = false
            }
        }
    }

    LaunchedEffect(isAnimatingSearch, isLoading, selectedMission) {
        if (gachaState == GachaState.Searching && !isAnimatingSearch && !isLoading) {
            if (selectedMission != null) {
                gachaState = GachaState.Revealing
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

            // Gunakan AnimatedContent untuk transisi halus antar UI
            AnimatedContent(
                targetState = gachaState == GachaState.NotFound,
                transitionSpec = {
                    if (targetState) {
                        // Masuk ke NotFound: Fade In + Scale In dengan Spring (Bouncy)
                        (fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                                scaleIn(
                                    initialScale = 0.8f,
                                    // PERBAIKAN DI SINI: Gunakan spring() bukan tween() untuk dampingRatio
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )).togetherWith(
                            fadeOut(animationSpec = tween(300))
                        )
                    } else {
                        // Keluar dari NotFound (Reset): Fade In biasa
                        fadeIn(animationSpec = tween(500)).togetherWith(
                            fadeOut(animationSpec = tween(300))
                        )
                    }
                },
                label = "NotFoundTransition"
            ) { isNotFound ->
                if (isNotFound) {
                    // --- HANDLING UI NOT FOUND ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Animasi Icon
                        val transition = rememberInfiniteTransition(label = "pulse")
                        val scale by transition.animateFloat(
                            initialValue = 1f, targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .scale(scale)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "Not Found",
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Misi Tidak Ditemukan",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Sepertinya belum ada misi yang cocok dengan preferensimu saat ini. Coba longgarkan kriteria pencarianmu.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = {
                                missionViewModel.clearError()
                                onNavigateToPreferences()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = DeepBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Ubah Preferensi", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    // --- UI GACHA NORMAL ---
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
                            else -> "" to ""
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
                    onSearchAgain = {
                        gachaState = GachaState.Idle
                        missionViewModel.clearSelectedMission()
                        onNavigateToPreferences()
                    },
                    onAccept = {
                        gachaState = GachaState.Finished
                    }
                )
            }

            // Tampilkan error snackbar jika bukan "Not Found" error
            error?.let {
                if (gachaState != GachaState.NotFound) {
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        action = { TextButton(onClick = { missionViewModel.clearError() }) { Text("OK") } },
                        containerColor = ErrorColor
                    ) { Text(it, color = Color.White) }
                }
            }
        }
    }
}

// ... (Components) ...
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

    val scale = remember { Animatable(1f) }

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
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        repeat(3) { cycle ->
            launch {
                rotation.animateTo(
                    targetValue = (cycle + 1) * 360f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )
            }
            launch {
                scale.animateTo(1.3f, tween(200))
                scale.animateTo(0.8f, tween(200))
            }
            launch {
                val angle = cycle * 120f
                val rad = Math.toRadians(angle.toDouble())
                offsetX.snapTo(kotlin.math.cos(rad).toFloat() * 50f)
                offsetY.snapTo(kotlin.math.sin(rad).toFloat() * 50f)
                offsetX.animateTo(0f, tween(400))
                offsetY.animateTo(0f, tween(400))
            }
            delay(800)
        }
        repeat(5) {
            rotation.animateTo(rotation.value + 10f, tween(50))
            rotation.animateTo(rotation.value - 10f, tween(50))
        }
        rotation.animateTo(0f, spring())
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
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(180f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
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
        delay(1000)
        onRevealFinished()
    }

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
        if (rotation.value > 270f) {
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
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepBlue)
            )
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
    val offsetY = remember { Animatable(particle.startY) }
    val offsetX = remember { Animatable(particle.startX) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(particle.delay.toLong())

        while (true) {
            offsetY.snapTo(particle.startY)
            offsetX.snapTo(particle.startX)
            alpha.snapTo(0f)
            launch { alpha.animateTo(0.8f, tween(300)) }
            launch {
                offsetY.animateTo(-200f, tween(particle.duration, easing = LinearEasing))
            }
            launch {
                val startTime = withFrameNanos { it }
                while (offsetY.value > -200f) {
                    val time = (withFrameNanos { it } - startTime) / 1_000_000f
                    val wave = kotlin.math.sin(time / 500f) * 20f
                    offsetX.snapTo(particle.startX + wave)
                }
            }
            delay(particle.duration - 300L)
            alpha.animateTo(0f, tween(300))
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