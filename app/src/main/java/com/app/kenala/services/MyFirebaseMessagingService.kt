package com.app.kenala.services

import android.content.Intent
import android.util.Log
import com.app.kenala.MainActivity
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.repository.NotificationRepository
import com.app.kenala.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        var title = "Pemberitahuan Kenala"
        var body = "Ada update terbaru!"

        remoteMessage.notification?.let {
            title = it.title ?: title
            body = it.body ?: body
        }

        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data["title"]?.let { if (it.isNotEmpty()) title = it }
            remoteMessage.data["body"]?.let { if (it.isNotEmpty()) body = it }
        }

        saveToDatabase(title, body)
        showNotification(title, body)
    }

    private fun saveToDatabase(title: String, body: String) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val repository = NotificationRepository(db.notificationDao())
                repository.saveNotification(title, body)
            } catch (e: Exception) {
                Log.e("FCM", "Error saving to DB", e)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showNotification(title, message)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}