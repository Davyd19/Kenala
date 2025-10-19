package com.app.kenala.screens.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kenala.ui.theme.BrightBlue
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryBlue

// --- Data Dummy Baru untuk Misi Multi-Langkah ---
private data class MissionStep(
    val step: Int,
    val totalSteps: Int,
    val clue: String,
    val locationQuery: String // Untuk Google Maps
)

private val missionSteps = listOf(
    MissionStep(1, 3, "Temukan mural besar yang menceritakan sejarah kota.", "Mural Sejarah Kota Padang"),
    MissionStep(2, 3, "Dari sana, cari kedai es durian legendaris yang selalu ramai.", "Es Durian Ganti Nan Lamo Padang"),
    MissionStep(3, 3, "Tujuan akhirmu adalah sebuah tugu ikonik di pusat kota.", "Tugu Perdamaian Padang")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    onGiveUpClick: () -> Unit,
    onArrivedClick: () -> Unit
) {
    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = missionSteps[currentStepIndex]
    val isLastStep = currentStepIndex == missionSteps.size - 1
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Panduan Misi") },
                navigationIcon = {
                    IconButton(onClick = onGiveUpClick) { // Tombol kembali juga dianggap menyerah
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

                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = "Ikon Misi",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = currentStep.clue,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
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
                        if (isLastStep) {
                            onArrivedClick()
                        } else {
                            currentStepIndex++
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = if (isLastStep) "SAYA SUDAH TIBA" else "LANGKAH BERIKUTNYA",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrightBlue
                    )
                }

                // Tombol Buka Peta
                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${currentStep.locationQuery}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        // Pastikan Google Maps terinstall sebelum menjalankan
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

