package com.app.kenala.screens.mission

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
// 1. TAMBAHKAN IMPORT EKSPLISIT
import androidx.compose.animation.core.RepeatMode
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
import com.app.kenala.data.remote.dto.CheckLocationResponse
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    missionId: String,
    onGiveUpClick: () -> Unit,
    onArrivedClick: () -> Unit,
    missionViewModel: MissionViewModel = viewModel()
) {
    val missionWithClues by missionViewModel.missionWithClues.collectAsState()
    val locationResponse by missionViewModel.checkLocationResponse.collectAsState()

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }
    val notificationHelper = remember { NotificationHelper(context) }

    val isLoading by missionViewModel.isLoading.collectAsState()
    val error by missionViewModel.error.collectAsState()

    // --- PERBAIKAN: Gunakan `locationResponse` untuk semua state ---
    val hasArrivedAtDestination = locationResponse?.destination?.isArrived == true
    val currentClue = locationResponse?.currentClue
    val destination = locationResponse?.destination
    // -------------------------------------------------------------

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    // 1. Request permissions on start
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    // 2. Ambil data misi & clues saat missionId tersedia
    // --- PERUBAHAN: Reset progress SEBELUM mengambil clues ---
    LaunchedEffect(missionId) {
        if (missionId != null) {
            // PANGGIL FUNGSI RESET BARU
            missionViewModel.resetMissionProgress(missionId) {
                // Setelah reset selesai, baru fetch clues
                missionViewModel.fetchMissionWithClues(missionId)
            }
        }
    }
    // -------------------------------------------------------

    // 3. Mulai location tracking
    // --- PERBAIKAN: Tambahkan `hasArrivedAtDestination` sebagai key ---
    LaunchedEffect(locationManager, missionWithClues, hasArrivedAtDestination) {
        if (missionWithClues == null) {
            // Jangan mulai tracking jika data misi (clues) belum siap
            return@LaunchedEffect
        }

        // --- PERBAIKAN: Jika sudah sampai (baik via skip/lokasi), hentikan location tracking ---
        if (hasArrivedAtDestination) {
            return@LaunchedEffect
        }
        // -------------------------------------------------------------------

        if (locationManager.hasLocationPermission()) {
            locationManager.getLocationUpdates().collectLatest { location ->
                // Panggil ViewModel untuk mengecek lokasi ke backend
                missionViewModel.checkLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
        }
    }

    // 4. Kirim notifikasi jika perlu
    LaunchedEffect(locationResponse) {
        val response = locationResponse ?: return@LaunchedEffect
        val missionName = missionWithClues?.mission?.name ?: "Misi"

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

        // Handle Loading (saat pertama kali memuat)
        if (isLoading && missionWithClues == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Handle Error
        if (error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        // Handle Misi tidak valid
        if (missionWithClues == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(text = "Misi tidak valid.")
            }
            return@Scaffold
        }

        // UI Utama
        Box(modifier = Modifier.fillMaxSize()) {
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
                            distanceMessage = if (hasArrivedAtDestination) {
                                "Anda Telah Tiba"
                            } else {
                                it.distance.formatted
                            },
                            hasArrived = hasArrivedAtDestination
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Text(
                        text = when {
                            hasArrivedAtDestination -> "🎉 Anda Telah Sampai!"
                            currentClue != null -> currentClue.description
                            destination != null -> "Semua petunjuk selesai! Menuju tujuan akhir: ${destination.name}"
                            else -> "Memuat petunjuk pertama..."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        color = if (hasArrivedAtDestination) ForestGreen else MaterialTheme.colorScheme.onBackground
                    )

                    if (hasArrivedAtDestination) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Siap menulis jurnal petualanganmu?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightTextColor,
                            textAlign = TextAlign.Center
                        )
                    } else if (currentClue != null && locationResponse?.clueReached == false) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Pesan: ${locationResponse?.distance?.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentColor,
                            fontWeight = FontWeight.SemiBold,
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
                    Button(
                        onClick = {
                            if (hasArrivedAtDestination) {
                                onArrivedClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasArrivedAtDestination) ForestGreen else PrimaryBlue
                        ),
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!hasArrivedAtDestination) {
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

                            // --- Tombol Skip Clue ---
                            TextButton(
                                onClick = { missionViewModel.skipCurrentClue() },
                                modifier = Modifier.height(56.dp),
                                enabled = !isLoading && currentClue != null // Aktif jika ada clue
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
                            // -------------------------
                        }
                    }
                }
            }

            // --- Loading Overlay ---
            // Tampil HANYA jika sedang loading TAPI data misi SUDAH ada
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
            // --- PERBAIKAN DI SINI ---
            repeatMode = RepeatMode.Reverse // Mengganti 'RepeatMode =' menjadi 'repeatMode ='
            // -------------------------
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