package com.app.kenala.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.kenala.utils.LocationManager
import com.app.kenala.utils.SocketManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject

class TrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationManager: LocationManager

    private var currentMissionId: Int = -1
    private var currentUserId: String = ""

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = LocationManager(applicationContext)
        SocketManager.connect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TRACKING" -> {
                currentMissionId = intent.getIntExtra("missionId", -1)
                currentUserId = intent.getStringExtra("userId") ?: ""

                startForegroundService()
                startLocationUpdates()
                Log.d("TrackingService", "Started tracking for Mission: $currentMissionId, User: $currentUserId")
            }
            "STOP_TRACKING" -> {
                stopTracking()
                Log.d("TrackingService", "Stopped tracking")
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "tracking_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Kenala Mission Active")
            .setContentText("Melacak perjalanan misi Anda...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(10f)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    sendLocationToSocket(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            Log.e("TrackingService", "Permission lost: ${e.message}")
        }
    }

    private fun sendLocationToSocket(location: Location) {
        if (currentMissionId != -1 && currentUserId.isNotEmpty()) {
            val data = JSONObject().apply {
                put("missionId", currentMissionId)
                put("userId", currentUserId)
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("timestamp", System.currentTimeMillis())
                put("speed", location.speed)
                put("heading", location.bearing)
            }

            // Emit ke Socket.IO
            // Pastikan event name 'update_location' sesuai dengan backend Anda
            SocketManager.emit("update_location", data)
        }
    }

    private fun stopTracking() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        serviceScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}