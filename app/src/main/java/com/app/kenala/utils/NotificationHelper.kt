package com.app.kenala.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.app.kenala.MainActivity
import com.app.kenala.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "kenala_location_channel"
        private const val CHANNEL_NAME = "Kenala Location Updates"
        private const val NOTIFICATION_ID = 1001

        const val ANNOUNCEMENT_CHANNEL_ID = "kenala_announcement_channel"
        private const val ANNOUNCEMENT_CHANNEL_NAME = "Pengumuman & Info"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_HIGH
            val locationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifikasi untuk update jarak ke tujuan"
            }
            notificationManager.createNotificationChannel(locationChannel)

            val announcementChannel = NotificationChannel(
                ANNOUNCEMENT_CHANNEL_ID,
                ANNOUNCEMENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi informasi umum dan maintenance"
            }
            notificationManager.createNotificationChannel(announcementChannel)
        }
    }

    fun showDistanceNotification(destinationName: String, distance: Double) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val distanceText = LocationManager.formatDistance(distance)
        val title = when {
            distance < 50 -> "🎉 Anda telah tiba!"
            distance < 100 -> "Hampir sampai!"
            distance < 500 -> "Semakin dekat!"
            else -> "Menuju tujuan"
        }

        val content = when {
            distance < 50 -> "Anda sudah sampai di $destinationName"
            else -> "Jarak ke $destinationName: $distanceText"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
        }
    }

    fun showArrivalNotification(destinationName: String) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎉 Selamat!")
            .setContentText("Anda telah sampai di $destinationName! Jangan lupa tulis jurnal petualanganmu.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID + 1, notification)
        } catch (e: SecurityException) {
        }
    }

    fun showNotification(title: String, message: String, intent: Intent? = null) {
        if (!hasNotificationPermission()) return

        val targetIntent = intent ?: Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            targetIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ANNOUNCEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}