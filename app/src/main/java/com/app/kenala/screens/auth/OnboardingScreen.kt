package com.app.kenala.screens.auth // Pastikan ini ada di dalam package auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.kenala.ui.theme.PrimaryColor
import com.app.kenala.ui.theme.PrimaryDark
import androidx.compose.ui.unit.sp

/**
 * Layar Onboarding (Selamat Datang)
 * Sesuai dengan Tampilan 1 di prototipe profesional.
 */
@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit // Parameter untuk navigasi
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Gambar Latar Belakang (dari prototipe)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://i.ibb.co/L5hY5M2/placeholder-nature.jpg")
                .crossfade(true)
                .build(),
            contentDescription = "Background Alam",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Gradien Gelap di Bagian Bawah
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PrimaryDark.copy(alpha = 0.8f),
                            PrimaryDark
                        ),
                        startY = 600f // Mulai gradien dari sekitar 60% layar
                    )
                )
        )

        // 3. Konten Teks dan Tombol
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 25.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Bottom // Taruh konten di bawah
        ) {
            // Judul
            Text(
                text = "Temukan Kejutan di Kotamu",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Deskripsi
            Text(
                text = "Bosan dengan rutinitas? Dapatkan misi acak ke tempat-tempat tersembunyi yang menantimu.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Tombol Aksi
            Button(
                onClick = onGetStartedClick, // Panggil fungsi navigasi
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large, // 16.dp
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = PrimaryColor
                )
            ) {
                Text(
                    text = "Mulai Petualangan",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}