package com.app.kenala.screens.mission

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.kenala.ui.theme.*
import com.app.kenala.utils.LocationManager
import com.app.kenala.utils.NotificationHelper
import com.app.kenala.viewmodel.MissionEvent
import com.app.kenala.viewmodel.MissionViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    missionId: String,
    onGiveUpClick: () -> Unit,
    onArrivedClick: (totalDistance: Double) -> Unit,
    missionViewModel: MissionViewModel = viewModel()
    // settingsViewModel telah dihapus
) {
    val missionWithClues by missionViewModel.missionWithClues.collectAsState()
    val locationResponse by missionViewModel.checkLocationResponse.collectAsState()
    // isLocationEnabledInSettings dan isNotificationsEnabledInSettings telah dihapus

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    val isLoading by missionViewModel.isLoading.collectAsState()
    val error by missionViewModel.error.collectAsState()

    // State UI
    val hasArrivedAtDestination = locationResponse?.destination?.isArrived == true
    val currentClue = locationResponse?.currentClue
    val destination = locationResponse?.destination

    // Logic Checks
    val isHeadingToDestination = currentClue == null && destination != null
    val totalClues = locationResponse?.progress?.total ?: 0
    val currentClueOrder = currentClue?.order ?: 0
    val isLastClue = currentClue != null && (currentClueOrder == totalClues)

    // Tracking Jarak Real
    var totalDistanceTraveled by remember { mutableDoubleStateOf(0.0) }
    var lastLocation by remember { mutableStateOf<android.location.Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    LaunchedEffect(missionId) {
        if (missionId != null) {
            missionViewModel.resetMissionProgress(missionId) {
                missionViewModel.fetchMissionWithClues(missionId)
            }
        }
    }

    // Logika Tracking (Polling GPS) - Hanya bergantung pada izin OS
    LaunchedEffect(locationManager, missionWithClues, hasArrivedAtDestination) {
        if (missionWithClues == null || hasArrivedAtDestination) return@LaunchedEffect

        if (locationManager.hasLocationPermission()) {
            locationManager.getLocationUpdates().collectLatest { location ->
                if (lastLocation != null) {
                    val distanceInc = lastLocation!!.distanceTo(location)
                    if (distanceInc > 2.0) {
                        totalDistanceTraveled += distanceInc
                    }
                }
                lastLocation = location

                missionViewModel.checkLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    // Notifikasi - Hanya bergantung pada izin OS
    LaunchedEffect(Unit) {
        missionViewModel.missionEvent.collectLatest { event ->
            when (event) {
                is MissionEvent.ShowNotification -> {
                    // event.title kini berisi pesan yang lebih akurat dari ViewModel
                    notificationHelper.showArrivalNotification(event.title)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panduan Misi", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Jarak ditempuh: ${"%.2f".format(totalDistanceTraveled / 1000)} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onGiveUpClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onGiveUpClick) {
                        Icon(Icons.Default.Flag, contentDescription = "Menyerah", tint = ErrorColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        // Blok pengecekan if (!isLocationEnabledInSettings) telah dihapus.

        if (isLoading && missionWithClues == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (missionWithClues == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { Text("Data misi tidak tersedia.") }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = missionWithClues?.mission?.name ?: "Memuat Misi...",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightTextColor,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    if (hasArrivedAtDestination) {
                        CheckmarkAnimation()
                    } else {
                        LocationIcon()
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // --- PERBAIKAN DISTANCE CARD ---
                    locationResponse?.let {
                        DistanceCard(
                            distanceMessage = if (hasArrivedAtDestination) "Anda Telah Tiba" else it.distance.formatted,
                            hasArrived = hasArrivedAtDestination
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        text = when {
                            hasArrivedAtDestination -> "🎉 Anda Telah Sampai!"
                            isHeadingToDestination -> "Menuju tujuan akhir: ${destination?.name}"
                            currentClue != null -> currentClue.description
                            else -> "Mencari sinyal GPS..."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        color = if (hasArrivedAtDestination) ForestGreen else MaterialTheme.colorScheme.onBackground
                    )

                    if (isLastClue && !hasArrivedAtDestination && !isHeadingToDestination) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Petunjuk terakhir! Tidak bisa dilewati.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorColor,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                    Button(
                        onClick = {
                            if (hasArrivedAtDestination) {
                                onArrivedClick(totalDistanceTraveled)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasArrivedAtDestination) ForestGreen else PrimaryBlue,
                            disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                        ),
                        // Tetap enable tombol tapi logic di onClick, atau ubah teks
                        enabled = true
                    ) {
                        if (hasArrivedAtDestination) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TULIS JURNAL", fontWeight = FontWeight.Bold)
                        } else {
                            // Indikator visual saja, tombol ini sebenarnya pasif sampai sampai
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MENUJU LOKASI...", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!hasArrivedAtDestination) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val targetLat = locationResponse?.currentClue?.latitude ?: locationResponse?.destination?.latitude
                                    val targetLon = locationResponse?.currentClue?.longitude ?: locationResponse?.destination?.longitude
                                    if (targetLat != null && targetLon != null) {
                                        val gmmIntentUri = Uri.parse("google.navigation:q=$targetLat,$targetLon")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("Peta", fontWeight = FontWeight.SemiBold)
                            }

                            val canSkip = !isLoading && currentClue != null && !isHeadingToDestination && !isLastClue
                            if (canSkip) {
                                TextButton(
                                    onClick = { missionViewModel.skipCurrentClue() },
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("Lewati", color = AccentColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ... (Fungsi CheckmarkAnimation, LocationIcon, dan DistanceCard tetap)
@Composable
fun CheckmarkAnimation() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }
    Box(
        modifier = Modifier.size(100.dp).scale(scale.value).background(ForestGreen.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.CheckCircle, "Berhasil", tint = ForestGreen, modifier = Modifier.size(64.dp))
    }
}

@Composable
fun LocationIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "pulse"
    )
    Box(
        modifier = Modifier.size(100.dp).background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Place, "Lokasi", tint = PrimaryBlue, modifier = Modifier.size(64.dp).scale(scale))
    }
}

@Composable
fun DistanceCard(distanceMessage: String, hasArrived: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (hasArrived) ForestGreen.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasArrived) ForestGreen.copy(alpha = 0.3f) else PrimaryBlue.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if(hasArrived) "Status" else "Jarak ke Petunjuk",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = distanceMessage,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (hasArrived) ForestGreen else PrimaryBlue
            )
        }
    }
}