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
import com.app.kenala.viewmodel.MissionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onArrivedClick: () -> Unit,
    missionViewModel: MissionViewModel = viewModel()
) {
    val selectedMission by missionViewModel.selectedMission.collectAsState()
    var currentDistance by remember { mutableStateOf<Double?>(null) }
    var hasArrived by remember { mutableStateOf(false) }
    var lastNotificationDistance by remember { mutableStateOf<Double?>(null) }

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

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

    // Location tracking - GUNAKAN DATA DARI selectedMission
    LaunchedEffect(selectedMission) {
        selectedMission?.let { mission ->
            if (locationManager.hasLocationPermission()) {
                locationManager.getLocationUpdates().collect { location ->
                    val distance = LocationManager.calculateDistance(
                        location.latitude,
                        location.longitude,
                        mission.latitude,
                        mission.longitude
                    )

                    currentDistance = distance

                    // Check if arrived (within 50 meters)
                    if (distance < 50 && !hasArrived) {
                        hasArrived = true
                        notificationHelper.showArrivalNotification(mission.name)
                    }

                    // Send notification at milestones
                    val shouldNotify = when {
                        distance < 100 && (lastNotificationDistance == null || lastNotificationDistance!! >= 100) -> true
                        distance < 500 && (lastNotificationDistance == null || lastNotificationDistance!! >= 500) -> true
                        distance < 1000 && (lastNotificationDistance == null || lastNotificationDistance!! >= 1000) -> true
                        else -> false
                    }

                    if (shouldNotify) {
                        notificationHelper.showDistanceNotification(mission.name, distance)
                        lastNotificationDistance = distance
                    }
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
        if (selectedMission == null) {
            // Show error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Tidak ada misi aktif",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Button(onClick = onGiveUpClick) {
                        Text("Kembali")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Content
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selectedMission!!.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightTextColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
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

                // Description or Arrival Message
                Text(
                    text = if (hasArrived) {
                        "🎉 Anda Telah Sampai!"
                    } else {
                        selectedMission!!.description ?: "Ikuti petunjuk menuju lokasi"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    color = if (hasArrived) ForestGreen else MaterialTheme.colorScheme.onBackground
                )

                if (hasArrived) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Siap menulis jurnal petualanganmu?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main progression button
                Button(
                    onClick = {
                        if (hasArrived) {
                            onArrivedClick()
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
                        text = if (hasArrived) "TULIS JURNAL" else "MENUJU LOKASI...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = WhiteColor
                    )
                }

                // Open map button
                if (!hasArrived && selectedMission != null) {
                    OutlinedButton(
                        onClick = {
                            val gmmIntentUri = Uri.parse(
                                "google.navigation:q=${selectedMission!!.latitude},${selectedMission!!.longitude}"
                            )
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
fun CheckmarkAnimation() {
    // Animasi rotasi dan skala untuk efek "berhasil"
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(360f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(800, easing = OvershootInterpolatorEasing))
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale.value)
            .rotate(rotation.value)
            .background(ForestGreen.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Berhasil",
            tint = ForestGreen,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun LocationIcon() {
    // Animasi berdenyut untuk ikon lokasi
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = LinearEasing),
            RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = "Lokasi",
            tint = PrimaryBlue,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun DistanceCard(distance: Double, hasArrived: Boolean) {
    val displayedDistance = when {
        distance >= 1000 -> String.format("%.2f km", distance / 1000)
        else -> String.format("%.0f m", distance)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasArrived) ForestGreen.copy(alpha = 0.1f)
            else PrimaryBlue.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jarak ke Tujuan",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextColor
            )
            Text(
                text = displayedDistance,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasArrived) ForestGreen else PrimaryBlue
            )
        }
    }
}

// Easing khusus untuk efek "overshoot"
val OvershootInterpolatorEasing = Easing { fraction ->
    val tension = 2.5f
    (fraction - 1).let { it * it * ((tension + 1) * it + tension) + 1 }
}
