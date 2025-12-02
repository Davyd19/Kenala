package com.app.kenala.ui.theme

import androidx.compose.ui.graphics.Color

// === EXISTING COLORS (Tetap) ===
val PrimaryYellow = Color(0xFFF8C104)
val YellowLight = Color(0xFFFDD835)
val YellowDark = Color(0xFFF9A825)

val SkyBlue = Color(0xFF0058A7)
val OceanBlue = Color(0xFF00398C)
val DeepBlue = Color(0xFF002364)
val MidnightBlue = Color(0xFF001845)

val ForestGreen = Color(0xFF004608)

val BackgroundLight = Color(0xFFF8F9FA)
val BackgroundWhite = Color(0xFFFFFFFF)
val TextPrimary = DeepBlue
val TextSecondary = Color(0xFF6B7280)
val BorderColor = Color(0xFFE5E7EB)
val ErrorColor = Color(0xFFDC2626)
val SuccessColor = ForestGreen

val GradientStart = OceanBlue
val GradientEnd = DeepBlue

// === FUNCTIONAL MAPPING (Light Mode) ===
val PrimaryColor = DeepBlue
val PrimaryDark = MidnightBlue
val SecondaryColor = SkyBlue
val AccentColor = PrimaryYellow
val BackgroundColor = BackgroundLight
val CardBackgroundColor = BackgroundWhite
val TextColor = TextPrimary
val LightTextColor = TextSecondary
val WhiteColor = BackgroundWhite
val DangerColor = ErrorColor

// === DARK MODE PALETTE (Revisi: Softer Eyes Version) ===

// Background: Tetap gelap untuk menghemat baterai & nuansa malam
val DarkBackground = Color(0xFF0B121E) // Sedikit lebih terang dari hitam pekat

// Surface (Kartu): DISINI KUNCINYA
// Sebelumnya terlalu gelap (0xFF0F172A).
// Sekarang kita ganti ke Navy-Abu yang lebih terang dan soft.
val DarkSurface = Color(0xFF1E2837)

// Surface Variant: Untuk elemen sekunder, lebih terang lagi
val DarkSurfaceVariant = Color(0xFF2D384A)

// Text: Putih gading (off-white) agar tidak terlalu tajam di mata
val DarkTextPrimary = Color(0xFFE2E8F0)
val DarkTextSecondary = Color(0xFF94A3B8)

// Accents:
val DarkAccent = Color(0xFFFFD54F)
val DarkSecondary = Color(0xFF38BDF8)
val DarkError = Color(0xFFEF5350)

// Deprecated
@Deprecated("Use AccentColor instead") val BrightBlue = PrimaryYellow
@Deprecated("Use SecondaryColor instead") val AccentBlue = SkyBlue
@Deprecated("Use PrimaryColor instead") val PrimaryBlue = DeepBlue
@Deprecated("Use TextSecondary instead") val LightBlue = TextSecondary
@Deprecated("Use MidnightBlue instead") val DarkBlue = MidnightBlue