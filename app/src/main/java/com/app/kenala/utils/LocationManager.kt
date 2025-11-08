package com.app.kenala.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            callback(null)
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                callback(location)
            }.addOnFailureListener {
                callback(null)
            }
        } catch (e: SecurityException) {
            callback(null)
        }
    }

    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // Update every 5 seconds
        ).apply {
            setMinUpdateIntervalMillis(2000L) // Fastest update every 2 seconds
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close()
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    companion object {
        fun calculateDistance(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }

        fun formatDistance(meters: Double): String {
            return when {
                meters < 1000 -> "${meters.toInt()} m"
                else -> String.format("%.1f km", meters / 1000)
            }
        }

        fun estimateTime(distanceMeters: Double, speedKmh: Double = 5.0): Int {
            // Default walking speed 5 km/h
            val hours = (distanceMeters / 1000) / speedKmh
            return (hours * 60).toInt() // Convert to minutes
        }
    }
}