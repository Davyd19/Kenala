package com.app.kenala.ui.theme // Pastikan package name ini sesuai

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Definisikan radius kustom sesuai prototipe
val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp), // Radius utama untuk tombol
    extraLarge = RoundedCornerShape(24.dp) // Radius untuk kartu
)