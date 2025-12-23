package com.app.kenala.data.repository

import com.app.kenala.data.local.dao.NotificationDao
import com.app.kenala.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    suspend fun saveNotification(title: String, body: String) {
        val notification = NotificationEntity(
            title = title,
            body = body,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notification)
    }

    suspend fun markAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearAll() {
        notificationDao.deleteAllNotifications()
    }
}