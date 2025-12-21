package com.app.kenala.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.app.kenala.MainActivity
import com.app.kenala.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Di sini nanti Anda bisa kirim token ke backend jika ingin mengirim notif ke user spesifik.
        // Tapi untuk broadcast, kita pakai Topic, jadi token ini opsional disimpan.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Cek jika pesan berisi notifikasi payload
        remoteMessage.notification?.let {
            val title = it.title ?: "Pemberitahuan Kenala"
            val body = it.body ?: "Ada update terbaru!"
            showNotification(title, body)
        }

        // Cek jika pesan berisi data payload (biasanya untuk custom logic)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            if (title != null && body != null) {
                showNotification(title, body)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationHelper = NotificationHelper(applicationContext)

        // Intent agar saat notif diklik, aplikasi terbuka
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Kita gunakan NotificationHelper yang sudah ada, tapi mungkin perlu sedikit penyesuaian
        // atau kita panggil fungsi manual di sini jika helper belum support custom text.
        // Asumsi NotificationHelper punya method dasar, atau kita buat notifikasi manual:

        notificationHelper.showSimpleNotification(title, message, intent)
    }
}