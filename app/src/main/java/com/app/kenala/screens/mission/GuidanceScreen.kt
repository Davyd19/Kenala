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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.app.kenala.services.TrackingService
import com.app.kenala.ui.theme.*
import com.app.kenala.utils.LocationManager
import com.app.kenala.utils.NotificationHelper
import com.app.kenala.viewmodel.MissionEvent
import com.app.kenala.viewmodel.MissionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    missionId: String,
    onGiveUpClick: () -> Unit,
    onArrivedClick: (totalDistance: Double) -> Unit,
    missionViewModel: MissionViewModel = viewModel()
) {
    val missionWithClues by missionViewModel.missionWithClues.collectAsState()
    val locationResponse by missionViewModel.checkLocationResponse.collectAsState()
    val distanceTraveled by missionViewModel.distanceTraveled.collectAsState()

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    val isLoading by missionViewModel.isLoading.collectAsState()
    val error by missionViewModel.error.collectAsState()

    val hasArrivedAtDestination = locationResponse?.destination?.isArrived == true
    val currentClue = locationResponse?.currentClue
    val destination = locationResponse?.destination

    val isHeadingToDestination = currentClue == null && destination != null
    val totalClues = locationResponse?.progress?.total ?: 0
    val currentClueOrder = currentClue?.order ?: 0
    val isLastClue = currentClue != null && (currentClueOrder == totalClues)

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
        if (missionId.isNotEmpty()) {
            missionViewModel.resetMissionProgress(missionId) {
                missionViewModel.fetchMissionWithClues(missionId)
            }
            val currentUserId = "user_placeholder_123"
            missionViewModel.startRealtimeTracking(missionId, currentUserId)

            val serviceIntent = Intent(context, TrackingService::class.java).apply {
                action = "START_TRACKING"
                try {
                    putExtra("missionId", missionId.toInt())
                } catch (e: NumberFormatException) {}
                putExtra("userId", currentUserId)
            }
            context.startForegroundService(serviceIntent)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val currentUserId = "user_placeholder_123"
            missionViewModel.stopRealtimeTracking(missionId, currentUserId)
            val serviceIntent = Intent(context, TrackingService::class.java).apply {
                action = "STOP_TRACKING"
            }
            context.startService(serviceIntent)
        }
    }

    LaunchedEffect(locationManager, missionWithClues, hasArrivedAtDestination) {
        if (missionWithClues == null || hasArrivedAtDestination) return@LaunchedEffect

        if (locationManager.hasLocationPermission()) {
            locationManager.getLocationUpdates().collectLatest { location ->
                missionViewModel.updateOdometer(location)
                missionViewModel.checkLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        missionViewModel.missionEvent.collectLatest { event ->
            when (event) {
                is MissionEvent.ShowNotification -> {
                    notificationHelper.showArrivalNotification(event.title)
                }
                is MissionEvent.MissionCompletedSuccessfully -> {
                    delay(2000)
                    onArrivedClick(distanceTraveled)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!hasArrivedAtDestination) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Panduan Misi", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Jarak ditempuh: ${"%.2f".format(distanceTraveled / 1000)} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightTextColor
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onGiveUpClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { innerPadding ->

        if (isLoading && missionWithClues == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (missionWithClues == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { Text("Data misi tidak tersedia.") }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (hasArrivedAtDestination) {
                ArrivalSuccessView(
                    modifier = Modifier.padding(innerPadding),
                    missionName = missionWithClues?.mission?.name ?: "Misi"
                )
            } else {
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

                        LocationIcon()

                        Spacer(modifier = Modifier.height(30.dp))

                        locationResponse?.let {
                            DistanceCard(
                                distanceMessage = it.distance.formatted,
                                hasArrived = false
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Text(
                            text = when {
                                isHeadingToDestination -> "Menuju tujuan akhir: ${destination?.name}"
                                currentClue != null -> currentClue.description
                                else -> "Mencari sinyal GPS..."
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (isLastClue && !isHeadingToDestination) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { /* Menunggu sampai */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor,
                                disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                            ),
                            enabled = true
                        ) {
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val targetLat = locationResponse?.currentClue?.latitude ?: locationResponse?.destination?.latitude
                                    val targetLon = locationResponse?.currentClue?.longitude ?: locationResponse?.destination?.longitude
                                    if (targetLat != null && targetLon != null) {
                                        val gmmIntentUri = Uri.parse("geo:$targetLat,$targetLon?q=$targetLat,$targetLon(Tujuan)")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")

                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$targetLat,$targetLon"))
                                            context.startActivity(browserIntent)
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

@Composable
fun ArrivalSuccessView(
    modifier: Modifier = Modifier,
    missionName: String
) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale.value)
                    .background(ForestGreen.copy(alpha = 0.1f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value)
                    .background(ForestGreen.copy(alpha = 0.2f), CircleShape)
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = ForestGreen,
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Selamat!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ForestGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Anda telah tiba di tujuan misi:",
            style = MaterialTheme.typography.bodyLarge,
            color = LightTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = missionName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = ForestGreen,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Menyimpan data & membuka jurnal...",
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextColor
        )
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
        modifier = Modifier.size(100.dp).background(PrimaryColor.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Place, "Lokasi", tint = PrimaryColor, modifier = Modifier.size(64.dp).scale(scale))
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
            containerColor = if (hasArrived) ForestGreen.copy(alpha = 0.15f) else PrimaryColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasArrived) ForestGreen.copy(alpha = 0.3f) else PrimaryColor.copy(alpha = 0.3f)
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
                color = if (hasArrived) ForestGreen else PrimaryColor
            )
        }
    }
}