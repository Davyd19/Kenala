package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.utils.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = DataStoreManager(application)

    // StateFlow agar UI bisa mengamati perubahan data secara real-time
    val notificationsEnabled: StateFlow<Boolean> = dataStore.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val locationEnabled: StateFlow<Boolean> = dataStore.locationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkModeEnabled: StateFlow<Boolean> = dataStore.darkModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setNotificationsEnabled(enabled)
        }
    }

    fun toggleLocation(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setLocationEnabled(enabled)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setDarkModeEnabled(enabled)
        }
    }
}