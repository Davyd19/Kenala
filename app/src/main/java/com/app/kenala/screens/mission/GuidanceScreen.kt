package com.app.kenala.screens.mission

import android.Manifest
import android.content.Intent
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
    onArrivedClick: () -> Unit,
    missionViewModel: MissionViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // 1. Ambil Data Misi & Lokasi dari ViewModel
    val missionWithClues by missionViewModel.missionWithClues.collectAsState()
    val locationResponse by missionViewModel.checkLocationResponse.collectAsState()

    // 2. Ambil Status Pengaturan (Real-time dari DataStore)
    val isLocationEnabledInSettings by settingsViewModel.locationEnabled.collectAsState()
    val isNotificationsEnabledInSettings by settingsViewModel.notificationsEnabled.collectAsState()

    // 3. Tools & Helper
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    val isLoading by missionViewModel.isLoading.collectAsState()
    val error by missionViewModel.error.collectAsState()

    // 4. State Logika UI
    val hasArrivedAtDestination = locationResponse?.destination?.isArrived == true
    val currentClue = locationResponse?.currentClue
    val destination = locationResponse?.destination

    // 5. Minta Izin Lokasi Android (Sistem)
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

    // 6. Muat Data Misi (Reset progress dulu baru fetch)
    LaunchedEffect(missionId) {
        if (missionId != null) {
            missionViewModel.resetMissionProgress(missionId) {
                missionViewModel.fetchMissionWithClues(missionId)
            }
        }
    }

    // 7. Logika Tracking Lokasi (Hanya jika diizinkan di Pengaturan)
    LaunchedEffect(locationManager, missionWithClues, hasArrivedAtDestination, isLocationEnabledInSettings) {
        // Jangan tracking jika data belum siap atau sudah sampai
        if (missionWithClues == null || hasArrivedAtDestination) return@LaunchedEffect

        // Cek Izin HP DAN Pengaturan Aplikasi
        if (locationManager.hasLocationPermission() && isLocationEnabledInSettings) {
            locationManager.getLocationUpdates().collectLatest { location ->
                missionViewModel.checkLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    // 8. Logika Notifikasi (Hanya jika diizinkan di Pengaturan)
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
                title = { Text("Panduan Misi") },
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

        // --- KONDISI 1: Jika Lokasi Dimatikan di Pengaturan ---
        if (!isLocationEnabledInSettings) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = ErrorColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Akses Lokasi Dimatikan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aktifkan lokasi di menu Pengaturan\nuntuk melanjutkan petualangan ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }

        // --- KONDISI 2: Loading Data Awal ---
        if (isLoading && missionWithClues == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // --- KONDISI 3: Error ---
        if (error != null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        // --- KONDISI 4: Data Kosong ---
        if (missionWithClues == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Misi tidak valid atau gagal dimuat.")
            }
            return@Scaffold
        }

        // --- UI UTAMA (GUIDANCE) ---
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
                    Spacer(modifier = Modifier.height(25.dp))

                    // Animasi Icon Tengah
                    if (hasArrivedAtDestination) {
                        CheckmarkAnimation()
                    } else {
                        LocationIcon()
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    // Kartu Jarak
                    locationResponse?.let {
                        DistanceCard(
                            distanceMessage = if (hasArrivedAtDestination)
                                "Anda Telah Tiba"
                            else
                                it.distance.formatted,
                            hasArrived = hasArrivedAtDestination
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Teks Petunjuk Utama
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

                    // Info Tambahan (Hint / Notifikasi Mati)
                    if (hasArrivedAtDestination) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Siap menulis jurnal petualanganmu?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightTextColor,
                            textAlign = TextAlign.Center
                        )
                    } else if (currentClue != null && locationResponse?.clueReached == false) {
                        // Tampilkan pesan jarak jika belum sampai
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = locationResponse?.distance?.message ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentColor,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Indikator kecil jika notifikasi dimatikan user
                    if (!isNotificationsEnabledInSettings && !hasArrivedAtDestination) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "Notifikasi suara dimatikan",
                                style = MaterialTheme.typography.labelSmall,
                                color = LightTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // --- TOMBOL AKSI BAWAH ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Utama (Berubah fungsi)
                    Button(
                        onClick = {
                            if (hasArrivedAtDestination) {
                                onArrivedClick() // Ke Jurnal
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasArrivedAtDestination) ForestGreen else PrimaryBlue
                        ),
                        // Tombol disable saat tracking, aktif saat sampai
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
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.large,
                                enabled = locationResponse != null && !isLoading
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(
                                    text = "Buka Peta",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Tombol Skip Clue
                            TextButton(
                                onClick = { missionViewModel.skipCurrentClue() },
                                modifier = Modifier.height(56.dp),
                                enabled = !isLoading && currentClue != null
                            ) {
                                Text(
                                    "Lewati",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isLoading && currentClue != null) AccentColor else LightTextColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Lewati Petunjuk",
                                    tint = if (!isLoading && currentClue != null) AccentColor else LightTextColor
                                )
                            }
                        }
                    }
                }
            }

            // Loading Overlay (saat Skip Clue misalnya)
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

// --- HELPER COMPOSABLES ---

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
        ), label = "pulse"
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
            modifier = Modifier.size(64.dp).scale(scale)
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
                text = if(hasArrived) "Status" else "Jarak ke Petunjuk",
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