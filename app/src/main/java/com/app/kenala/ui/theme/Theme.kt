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

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DeepBlue,
    primaryContainer = DeepBlue,
    onPrimaryContainer = DarkAccent,
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    tertiary = PrimaryYellow,
    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkSurfaceVariant.copy(alpha = 0.8f),
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

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