package com.app.kenala.screens.mission

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.kenala.R
import com.app.kenala.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

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
    Idle, ReadyToPull, Searching, Finished
}

@Composable
fun GachaScreen(onMissionFound: () -> Unit) {
    var gachaState by remember { mutableStateOf(GachaState.Idle) }
    var revealedMission by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

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
            // Enhanced Animated Background Particles
            EnhancedFloatingParticles(isActive = gachaState == GachaState.Searching)

            // Ambient floating particles always visible
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
                    GachaState.Finished -> Triple("🎉 Misi Ditemukan!", revealedMission ?: "", "")
                }

                AnimatedText(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (gachaState == GachaState.Finished) AccentColor else Color.White
                )

                if (subtitle.isNotEmpty()) {
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

                // Interactive Card Container
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
                                        dragOffset = 0f
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
                            EpicShufflingCards(
                                onAnimationComplete = {
                                    revealedMission = dummyMissions.random()
                                    gachaState = GachaState.Finished
                                }
                            )
                        }
                        GachaState.Finished -> {
                            revealedMission?.let { mission ->
                                EpicRevealCard(missionName = mission)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Button
                if (gachaState == GachaState.Finished) {
                    AnimatedButton(
                        text = "LIHAT MISI",
                        onClick = onMissionFound
                    )
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
private fun AnimatedButton(text: String, onClick: () -> Unit) {
    var scale by remember { mutableFloatStateOf(0.8f) }

    LaunchedEffect(Unit) {
        animate(0.8f, 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { value, _ ->
            scale = value
        }
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 40.dp)
            .scale(scale),
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
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
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
            // Fade in
            animate(0f, 0.8f, animationSpec = tween(300)) { value, _ ->
                alpha = value
            }

            // Move up with slight horizontal sway
            val endX = particle.startX + Random.nextFloat() * 100f - 50f

            animate(
                initialValue = particle.startY,
                targetValue = -200f,
                animationSpec = tween(particle.duration, easing = LinearEasing)
            ) { value, _ ->
                offsetY = value
                // Add sway effect
                offsetX = particle.startX + kotlin.math.sin(value / 50f) * 20f
            }

            // Fade out
            animate(0.8f, 0f, animationSpec = tween(300)) { value, _ ->
                alpha = value
            }

            // Reset
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
        // Phase 1: Wild spinning and movement
        repeat(3) { cycle ->
            // Spin dramatically
            animate(0f, 720f, animationSpec = tween(800, easing = FastOutSlowInEasing)) { value, _ ->
                rotation = value
            }

            // Scale pulse
            animate(1f, 1.3f, animationSpec = tween(200)) { value, _ ->
                scale = value
            }
            animate(1.3f, 0.8f, animationSpec = tween(200)) { value, _ ->
                scale = value
            }

            // Circular motion
            val angle = cycle * 120f
            animate(0f, 60f, animationSpec = tween(400)) { value, _ ->
                offsetX = kotlin.math.cos(Math.toRadians((angle + value).toDouble())).toFloat() * 50f
                offsetY = kotlin.math.sin(Math.toRadians((angle + value).toDouble())).toFloat() * 50f
            }
        }

        // Phase 2: Return to center with multiple flips
        animate(offsetX, 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { value, _ ->
            offsetX = value
        }
        animate(offsetY, 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { value, _ ->
            offsetY = value
        }

        // Phase 3: Final rapid flips
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

        // Phase 4: Fade out
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
        // Epic entrance
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

        // Settle
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