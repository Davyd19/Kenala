package com.app.kenala.screens.mission // Pastikan ini ada di dalam package mission

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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kenala.ui.theme.BorderColor
import com.app.kenala.ui.theme.LightTextColor

/**
 * Layar Panduan Misi
 * Sesuai dengan Tampilan 5 di prototipe profesional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(
    onCancelClick: () -> Unit,
    onArrivedClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            // Top App Bar dengan tombol kembali
            TopAppBar(
                title = {
                    Text(
                        "Panduan Misi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Batalkan Misi")
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
            // Konten di tengah (kita gunakan Spacer untuk mendorongnya)
            Column(
                modifier = Modifier.weight(1f), // Ambil semua sisa ruang
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Teks progres
                Text(
                    text = "Langkah 1 dari 4", // Data dummy
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(25.dp))

                // Ikon Petunjuk (dari prototipe)
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = "Ikon Misi",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(25.dp))

                // Teks Petunjuk
                Text(
                    text = "Temukan tugu ikonik di pusat kota.", // Data dummy
                    style = MaterialTheme.typography.headlineSmall, // 24px
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
            }

            // Tombol Aksi (di bagian bawah)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp), // Padding dari bawah
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Tombol Batalkan
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .weight(1f) // Lebar 1/3
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large, // 16dp
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BorderColor,
                        contentColor = LightTextColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Batalkan")
                }

                // Tombol "Saya Sudah di Sini"
                Button(
                    onClick = onArrivedClick,
                    modifier = Modifier
                        .weight(2f) // Lebar 2/3
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large, // 16dp
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Saya Sudah di Sini",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}