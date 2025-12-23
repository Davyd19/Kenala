package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = NotificationRepository(db.notificationDao())
    }

    val notifications = repository.allNotifications.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

}