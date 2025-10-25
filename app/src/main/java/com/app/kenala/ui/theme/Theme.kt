package com.app.kenala.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // Primary - Menggunakan Deep Blue sebagai primary
    primary = PrimaryColor,
    onPrimary = WhiteColor,
    primaryContainer = OceanBlue,
    onPrimaryContainer = WhiteColor,

    // Secondary - Sky Blue
    secondary = SecondaryColor,
    onSecondary = WhiteColor,
    secondaryContainer = SkyBlue.copy(alpha = 0.1f),
    onSecondaryContainer = DeepBlue,

    // Tertiary - Yellow Accent
    tertiary = AccentColor,
    onTertiary = DeepBlue,
    tertiaryContainer = PrimaryYellow.copy(alpha = 0.1f),
    onTertiaryContainer = DeepBlue,

    // Background
    background = BackgroundColor,
    onBackground = TextColor,

    // Surface
    surface = CardBackgroundColor,
    onSurface = TextColor,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,

    // Surface Tint for elevated surfaces
    surfaceTint = PrimaryColor.copy(alpha = 0.05f),

    // Outline & Borders
    outline = BorderColor,
    outlineVariant = BorderColor.copy(alpha = 0.5f),

    // Error
    error = ErrorColor,
    onError = WhiteColor,
    errorContainer = ErrorColor.copy(alpha = 0.1f),
    onErrorContainer = ErrorColor,

    // Inverse colors for dark elements on light background
    inverseSurface = DeepBlue,
    inverseOnSurface = WhiteColor,
    inversePrimary = PrimaryYellow,

    // Scrim for modals
    scrim = Color.Black.copy(alpha = 0.5f)
)

@Composable
fun KenalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to match background
            window.statusBarColor = colorScheme.background.toArgb()
            // Set navigation bar color
            window.navigationBarColor = colorScheme.surface.toArgb()

            // Set status bar icons to dark (since we have light background)
            WindowCompat.getInsetsController(window, view).apply {
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