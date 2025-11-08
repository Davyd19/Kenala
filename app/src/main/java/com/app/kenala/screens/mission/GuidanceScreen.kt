package com.app.kenala.screens.mission

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kenala.ui.theme.*
import com.app.kenala.utils.LocationManager
import com.app.kenala.utils.NotificationHelper
import kotlinx.coroutines.delay

private data class MissionStep(
    val step: Int,
    val totalSteps: Int,
    val clue: String,
    val locationQuery: String,
    val latitude: Double,
    val longitude: Double
)

private val missionSteps = listOf(
    MissionStep(1, 3, "Temukan mural besar yang menceritakan sejarah kota.", "Mural Sejarah Kota Padang", -0.9471, 100.4172),
    MissionStep(2, 3, "Dari sana, cari kedai es durian legendaris yang selalu ramai.", "Es Durian Ganti Nan Lamo Padang", -0.9489, 100.4183),
    MissionStep(3, 3, "Tujuan akhirmu adalah sebuah tugu ikonik di pusat kota.", "Tugu Perdamaian Padang", -0.9500, 100.4194)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    onGiveUpClick: () -> Unit,
    onArrivedClick: () -> Unit
) {
    var currentStepIndex by remember { mutableStateOf(0) }
    var currentDistance by remember { mutableStateOf<Double?>(null) }
    var hasArrived by remember { mutableStateOf(false) }
    var lastNotificationDistance by remember { mutableStateOf<Double?>(null) }

    val currentStep = missionSteps[currentStepIndex]
    val isLastStep = currentStepIndex == missionSteps.size - 1
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission result
    }

    // Request permissions on start
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    // Location tracking
    LaunchedEffect(currentStep) {
        if (locationManager.hasLocationPermission()) {
            locationManager.getLocationUpdates().collect { location ->
                val distance = LocationManager.calculateDistance(
                    location.latitude,
                    location.longitude,
                    currentStep.latitude,
                    currentStep.longitude
                )

                currentDistance = distance

                // Check if arrived (within 50 meters)
                if (distance < 50 && !hasArrived) {
                    hasArrived = true
                    notificationHelper.showArrivalNotification(currentStep.locationQuery)
                }

                // Send notification at milestones
                val shouldNotify = when {
                    distance < 100 && (lastNotificationDistance == null || lastNotificationDistance!! >= 100) -> true
                    distance < 500 && (lastNotificationDistance == null || lastNotificationDistance!! >= 500) -> true
                    distance < 1000 && (lastNotificationDistance == null || lastNotificationDistance!! >= 1000) -> true
                    else -> false
                }

                if (shouldNotify) {
                    notificationHelper.showDistanceNotification(currentStep.locationQuery, distance)
                    lastNotificationDistance = distance
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Panduan Misi") },
                navigationIcon = {
                    IconButton(onClick = onGiveUpClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali & Batalkan Misi")
                    }
                },
                actions = {
                    IconButton(onClick = onGiveUpClick) {
                        Icon(Icons.Default.Flag, contentDescription = "Menyerah")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Konten di tengah
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Langkah ${currentStep.step} dari ${currentStep.totalSteps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(25.dp))

                // Status Icon with Animation
                if (hasArrived) {
                    CheckmarkAnimation()
                } else {
                    LocationIcon()
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Distance Display
                currentDistance?.let { distance ->
                    DistanceCard(
                        distance = distance,
                        hasArrived = hasArrived
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Clue or Arrival Message
                Text(
                    text = if (hasArrived) {
                        "🎉 Anda Telah Sampai!"
                    } else {
                        currentStep.clue
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    color = if (hasArrived) ForestGreen else MaterialTheme.colorScheme.onBackground
                )

                if (hasArrived && !isLastStep) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Siap melanjutkan ke langkah berikutnya?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tombol Aksi di bawah
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tombol Progresi Utama
                Button(
                    onClick = {
                        if (isLastStep && hasArrived) {
                            onArrivedClick()
                        } else if (hasArrived) {
                            currentStepIndex++
                            hasArrived = false
                            currentDistance = null
                            lastNotificationDistance = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasArrived) ForestGreen else PrimaryBlue
                    ),
                    enabled = hasArrived
                ) {
                    Icon(
                        if (hasArrived) Icons.Default.Check else Icons.Default.LocationSearching,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isLastStep && hasArrived -> "TULIS JURNAL"
                            hasArrived -> "LANGKAH BERIKUTNYA"
                            else -> "MENUJU LOKASI..."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = WhiteColor
                    )
                }

                // Tombol Buka Peta
                if (!hasArrived) {
                    OutlinedButton(
                        onClick = {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${currentStep.locationQuery}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Buka Peta",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "location")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .background(OceanBlue.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Ikon Misi",
            modifier = Modifier.size(48.dp),
            tint = OceanBlue
        )
    }
}

@Composable
private fun CheckmarkAnimation() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "checkRotation"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .rotate(rotation)
            .background(ForestGreen.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Sampai",
            modifier = Modifier.size(48.dp),
            tint = ForestGreen
        )
    }
}

@Composable
private fun DistanceCard(distance: Double, hasArrived: Boolean) {
    val distanceText = LocationManager.formatDistance(distance)
    val backgroundColor = when {
        hasArrived -> ForestGreen
        distance < 100 -> AccentColor
        distance < 500 -> OceanBlue
        else -> PrimaryBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (hasArrived) "Anda sudah sampai!" else "Jarak ke tujuan",
                style = MaterialTheme.typography.bodyMedium,
                color = backgroundColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = distanceText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            if (!hasArrived && distance < 100) {
                Text(
                    text = "Hampir sampai!",
                    style = MaterialTheme.typography.bodySmall,
                    color = backgroundColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}