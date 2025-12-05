package com.app.kenala.screens.mission

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.app.kenala.viewmodel.MissionViewModel
import com.app.kenala.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    missionId: String,
    onGiveUpClick: () -> Unit,
    // Update callback menerima jarak
    onArrivedClick: (totalDistance: Double) -> Unit,
    missionViewModel: MissionViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // 1. Ambil Data Misi & Lokasi dari ViewModel
    val missionWithClues by missionViewModel.missionWithClues.collectAsState()
    val locationResponse by missionViewModel.checkLocationResponse.collectAsState()

    // 2. Ambil Status Pengaturan
    val isLocationEnabledInSettings by settingsViewModel.locationEnabled.collectAsState()
    val isNotificationsEnabledInSettings by settingsViewModel.notificationsEnabled.collectAsState()

    // 3. Tools & Helper
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    val isLoading by missionViewModel.isLoading.collectAsState()
    val error by missionViewModel.error.collectAsState()

    // 4. State Logika UI
    // Pastikan status "Tiba" benar-benar dari backend (isArrived)
    val hasArrivedAtDestination = locationResponse?.destination?.isArrived == true
    val currentClue = locationResponse?.currentClue
    val destination = locationResponse?.destination

    // --- FITUR BARU: Tracking Jarak Real ---
    var totalDistanceTraveled by remember { mutableDoubleStateOf(0.0) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    // ---------------------------------------

    // 5. Minta Izin Lokasi
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

    // 6. Muat Data Misi
    LaunchedEffect(missionId) {
        if (missionId != null) {
            missionViewModel.resetMissionProgress(missionId) {
                missionViewModel.fetchMissionWithClues(missionId)
            }
        }
    }

    // 7. Logika Tracking Lokasi & Hitung Jarak Real
    LaunchedEffect(
        locationManager,
        missionWithClues,
        hasArrivedAtDestination,
        isLocationEnabledInSettings
    ) {
        if (missionWithClues == null || hasArrivedAtDestination) return@LaunchedEffect

        if (locationManager.hasLocationPermission() && isLocationEnabledInSettings) {
            locationManager.getLocationUpdates().collectLatest { location ->
                // Hitung akumulasi jarak
                if (lastLocation != null) {
                    val distanceInc = lastLocation!!.distanceTo(location) // returns meters
                    // Hanya tambahkan jika perpindahan masuk akal (misal > 2m untuk filter noise GPS saat diam)
                    if (distanceInc > 2.0) {
                        totalDistanceTraveled += distanceInc
                    }
                }
                lastLocation = location

                // Kirim ke backend untuk cek progress
                missionViewModel.checkLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    // 8. Logika Notifikasi
    LaunchedEffect(locationResponse) {
        val response = locationResponse ?: return@LaunchedEffect
        if (isNotificationsEnabledInSettings) {
            if (response.status == "clue_reached") {
                notificationHelper.showDistanceNotification(
                    response.currentClue?.name ?: "Petunjuk", 0.0
                )
            }
            if (response.destination?.isArrived == true) {
                notificationHelper.showArrivalNotification(
                    response.destination.name
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panduan Misi")
                        // Tampilkan jarak tempuh real-time (optional, buat debugging user)
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
                        Icon(Icons.Default.Flag, contentDescription = "Menyerah")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->

        // Handle Permissions & Loading/Error States
        if (!isLocationEnabledInSettings) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Lokasi tidak aktif. Mohon aktifkan GPS.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            return@Scaffold
        }
        if (isLoading && missionWithClues == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        // Tampilkan error sebagai snackbar/text, tapi jangan halangi UI utama jika masih bisa tracking
        if (missionWithClues == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Data misi tidak tersedia.") }
            return@Scaffold
        }

        // --- UI UTAMA ---
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Area konten tengah
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
                    Spacer(modifier = Modifier.height(25.dp))

                    if (hasArrivedAtDestination) {
                        CheckmarkAnimation()
                    } else {
                        LocationIcon()
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    locationResponse?.let {
                        DistanceCard(
                            distanceMessage = if (hasArrivedAtDestination) "Anda Telah Tiba" else it.distance.formatted,
                            hasArrived = hasArrivedAtDestination
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Text(
                        text = when {
                            hasArrivedAtDestination -> "🎉 Anda Telah Sampai!"
                            currentClue != null -> currentClue.description
                            destination != null -> "Menuju tujuan akhir: ${destination.name}"
                            else -> "Memuat petunjuk..."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        color = if (hasArrivedAtDestination) ForestGreen else MaterialTheme.colorScheme.onBackground
                    )

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // --- TOMBOL AKSI ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Utama (Tulis Jurnal)
                    Button(
                        onClick = {
                            if (hasArrivedAtDestination) {
                                // Kirim total jarak tempuh ke Jurnal
                                onArrivedClick(totalDistanceTraveled)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasArrivedAtDestination) ForestGreen else PrimaryBlue
                        ),
                        // DISABLED JIKA BELUM SAMPAI (Anti-Exploit)
                        enabled = hasArrivedAtDestination
                    ) {
                        Icon(
                            if (hasArrivedAtDestination) Icons.Default.Check else Icons.Default.LocationSearching,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasArrivedAtDestination) "TULIS JURNAL" else "MENUJU LOKASI...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = WhiteColor
                        )
                    }

                    // Tombol Sekunder (Peta & Skip) - Hanya muncul jika belum sampai
                    if (!hasArrivedAtDestination) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tombol Buka Peta
                            OutlinedButton(
                                onClick = {
                                    val targetLat = locationResponse?.currentClue?.latitude
                                        ?: locationResponse?.destination?.latitude
                                    val targetLon = locationResponse?.currentClue?.longitude
                                        ?: locationResponse?.destination?.longitude
                                    if (targetLat != null && targetLon != null) {
                                        val gmmIntentUri =
                                            Uri.parse("google.navigation:q=$targetLat,$targetLon")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(mapIntent)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.large,
                                enabled = locationResponse != null && !isLoading
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("Buka Peta", fontWeight = FontWeight.SemiBold)
                            }

                            // --- LOGIKA SKIP ---
                            // Cek apakah ini langkah terakhir? (Sedang OTW destination atau status all_clues_completed)
                            val isHeadingToDestination =
                                locationResponse?.status == "all_clues_completed" || currentClue == null

                            // Skip button DISABLED jika sedang menuju destination (Langkah Terakhir)
                            val canSkip =
                                !isLoading && currentClue != null && !isHeadingToDestination

                            TextButton(
                                onClick = { missionViewModel.skipCurrentClue() },
                                modifier = Modifier.height(56.dp),
                                enabled = canSkip
                            ) {
                                Text(
                                    "Lewati",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canSkip) AccentColor else LightTextColor.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Lewati Petunjuk",
                                    tint = if (canSkip) AccentColor else LightTextColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Loading Overlay
            if (isLoading && missionWithClues != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

// Helper Composables
@Composable
fun CheckmarkAnimation() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale.value)
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
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = "Lokasi",
            tint = PrimaryBlue,
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
        )
    }
}

@Composable
fun DistanceCard(distanceMessage: String, hasArrived: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasArrived) ForestGreen.copy(alpha = 0.1f) else PrimaryBlue.copy(
                alpha = 0.1f
            )
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
                text = if (hasArrived) "Status" else "Jarak ke Petunjuk",
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextColor
            )
            Text(
                text = distanceMessage,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (hasArrived) ForestGreen else PrimaryBlue
            )
        }
    }
}