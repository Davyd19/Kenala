package com.app.kenala.screens.auth // Pastikan ini ada di dalam package auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.app.kenala.R // Kita akan butuh ini untuk logo Google
import com.app.kenala.ui.theme.LightTextColor
import com.app.kenala.ui.theme.PrimaryColor

/**
 * Layar Login
 * Sesuai dengan Tampilan 2 di prototipe profesional.
 */
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Scaffold menyediakan area dasar untuk aplikasi
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Padding dari Scaffold
                .padding(horizontal = 25.dp), // Padding konten kita
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Judul dan Subjudul
            Text(
                text = "Selamat Datang",
                style = MaterialTheme.typography.headlineMedium, // 28px SemiBold
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Masuk untuk melanjutkan petualanganmu.",
                style = MaterialTheme.typography.bodyLarge, // 16px
                color = LightTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2. Tombol Masuk dengan Google
            Button(
                onClick = onLoginClick, // Panggil navigasi ke MainScreen
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large, // 16dp
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Putih
                    contentColor = MaterialTheme.colorScheme.onSurface // Hitam
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp, // Sedikit bayangan
                    pressedElevation = 0.dp
                )
            ) {
                /*
                // UNTUK NANTI: Tambahkan logo Google
                // 1. Download logo Google (format .png atau .xml)
                // 2. Taruh di folder res/drawable
                // 3. Hapus komentar di bawah ini

                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo), // Ganti ini
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                */

                Text(
                    text = "Masuk dengan Google",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Teks "atau Daftar dengan Email"
            TextButton(onClick = onRegisterClick) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = LightTextColor)) {
                            append("atau ")
                        }
                        withStyle(style = SpanStyle(
                            color = PrimaryColor,
                            fontWeight = FontWeight.SemiBold
                        )) {
                            append("Daftar dengan Email")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}