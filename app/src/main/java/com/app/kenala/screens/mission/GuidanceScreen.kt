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
import androidx.compose.material3.OutlinedButton
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
import com.app.kenala.ui.theme.BrightBlue
import com.app.kenala.ui.theme.LightBlue
import com.app.kenala.ui.theme.PrimaryBlue
import com.app.kenala.ui.theme.PrimaryDark

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
                        fontWeight = FontWeight.Bold // Dibuat lebih tebal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Batalkan Misi",
                            tint = PrimaryBlue // Warna ikon yang konsisten
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
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
                    text = "LANGKAH 1 DARI 4", // Data dummy, dibuat kapital
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlue,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp // Sedikit jarak antar huruf
                )
                Spacer(modifier = Modifier.height(30.dp))

                // Ikon Petunjuk (dari prototipe)
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = "Ikon Misi",
                    modifier = Modifier.size(80.dp), // Ikon lebih besar
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(30.dp))

                // Teks Petunjuk
                Text(
                    text = "Temukan tugu ikonik di pusat kota.", // Data dummy
                    style = MaterialTheme.typography.headlineMedium, // 28px
                    fontWeight = FontWeight.Bold, // Lebih tebal untuk penekanan
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    color = PrimaryDark
                )
            }

            // Tombol Aksi (di bagian bawah)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp), // Padding dari bawah
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Tombol Batalkan (Outlined)
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Batalkan Misi")
                }

                // Tombol "Saya Sudah di Sini" (Filled)
                Button(
                    onClick = onArrivedClick,
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = BrightBlue
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "SAYA SUDAH TIBA",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
