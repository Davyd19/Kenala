package com.app.kenala.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Light Mode (Tetap Sama/Original)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = WhiteColor,
    primaryContainer = OceanBlue,
    onPrimaryContainer = WhiteColor,
    secondary = SecondaryColor,
    onSecondary = WhiteColor,
    tertiary = AccentColor,
    onTertiary = DeepBlue,
    background = BackgroundColor,
    onBackground = TextColor,
    surface = CardBackgroundColor,
    onSurface = TextColor,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = ErrorColor,
    onError = WhiteColor
)

// 2. Dark Mode (Disempurnakan: Night Adventure Theme) 🌙
private val DarkColorScheme = darkColorScheme(
    // Primary: Gunakan Kuning agar ikon/tombol aktif 'menyala' di kegelapan
    primary = DarkAccent,
    onPrimary = DeepBlue, // Teks di atas tombol kuning tetap biru tua agar terbaca

    // Container: Area berwarna (seperti tombol non-aktif/chip) pakai biru tua
    primaryContainer = DeepBlue,
    onPrimaryContainer = DarkAccent,

    // Secondary: Biru muda bercahaya (Cyan/Sky)
    secondary = DarkSecondary,
    onSecondary = DarkBackground,

    // Tertiary: Variasi lain
    tertiary = PrimaryYellow,

    // Background & Surface
    background = DarkBackground,    // Biru Malam Sangat Gelap
    onBackground = DarkTextPrimary, // Teks Putih Tulang

    surface = DarkSurface,          // Kartu (Navy Gelap)
    onSurface = DarkTextPrimary,    // Teks di atas Kartu

    surfaceVariant = DarkSurfaceVariant, // Kartu sekunder
    onSurfaceVariant = DarkTextSecondary, // Teks sekunder (Abu kebiruan)

    outline = DarkSurfaceVariant.copy(alpha = 0.8f), // Garis pembatas halus
    error = DarkError,
    onError = DarkBackground
)

@Composable
fun KenalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar mengikuti warna background agar terlihat seamless (edge-to-edge)
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                // Saat Dark Mode, ikon status bar harus terang (isLight = false)
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}