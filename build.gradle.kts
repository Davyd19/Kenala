// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Tambahkan plugin google services (sesuaikan versi jika perlu, 4.4.4 cukup stabil)
    id("com.google.gms.google-services") version "4.4.4" apply false
    // Tambahkan plugin KSP di sini juga agar bisa digunakan di sub-project
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}