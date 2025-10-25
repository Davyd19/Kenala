package com.app.kenala.ui.theme

import androidx.compose.ui.graphics.Color

// === NEW COLOR PALETTE ===
// Primary Colors - Kuning sebagai aksen utama
val PrimaryYellow = Color(0xFFF8C104)      // Kuning cerah - untuk CTA & highlights
val YellowLight = Color(0xFFFDD835)        // Kuning terang - untuk hover states
val YellowDark = Color(0xFFF9A825)         // Kuning gelap - untuk pressed states

// Blue Scale - Dari terang ke gelap
val SkyBlue = Color(0xFF0058A7)            // Biru terang - untuk secondary actions
val OceanBlue = Color(0xFF00398C)          // Biru medium - untuk backgrounds
val DeepBlue = Color(0xFF002364)           // Biru gelap - untuk text & primary elements
val MidnightBlue = Color(0xFF001845)       // Biru sangat gelap - untuk dark backgrounds

// Green Accent
val ForestGreen = Color(0xFF004608)        // Hijau gelap - untuk success states

// Neutral & Support Colors
val BackgroundLight = Color(0xFFF8F9FA)    // Background terang
val BackgroundWhite = Color(0xFFFFFFFF)    // Card backgrounds
val TextPrimary = DeepBlue                 // Text utama
val TextSecondary = Color(0xFF6B7280)      // Text sekunder
val BorderColor = Color(0xFFE5E7EB)        // Borders
val ErrorColor = Color(0xFFDC2626)         // Error states
val SuccessColor = ForestGreen             // Success states

// Gradient Colors
val GradientStart = OceanBlue
val GradientEnd = DeepBlue

// === FUNCTIONAL COLOR MAPPING ===
val PrimaryColor = DeepBlue                // Primary brand color
val PrimaryDark = MidnightBlue             // Darker variant
val SecondaryColor = SkyBlue               // Secondary actions
val AccentColor = PrimaryYellow            // Accent & highlights
val BackgroundColor = BackgroundLight
val CardBackgroundColor = BackgroundWhite
val TextColor = TextPrimary
val LightTextColor = TextSecondary
val WhiteColor = BackgroundWhite
val DangerColor = ErrorColor

// Deprecated colors (for backward compatibility)
@Deprecated("Use AccentColor instead", ReplaceWith("AccentColor"))
val BrightBlue = PrimaryYellow
@Deprecated("Use SecondaryColor instead", ReplaceWith("SecondaryColor"))
val AccentBlue = SkyBlue
@Deprecated("Use PrimaryColor instead", ReplaceWith("PrimaryColor"))
val PrimaryBlue = DeepBlue
@Deprecated("Use TextSecondary instead", ReplaceWith("TextSecondary"))
val LightBlue = TextSecondary
@Deprecated("Use MidnightBlue instead", ReplaceWith("MidnightBlue"))
val DarkBlue = MidnightBlue