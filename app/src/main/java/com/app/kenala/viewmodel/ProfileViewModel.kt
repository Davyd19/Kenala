package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.UserEntity
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userRepository = UserRepository(
        RetrofitClient.apiService,
        database.userDao()
    )

    // State untuk data user dari database lokal
    val user: StateFlow<UserEntity?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // State untuk data statistik dari API
    private val _stats = MutableStateFlow<StatsDto?>(null)
    val stats: StateFlow<StatsDto?> = _stats.asStateFlow()

    // State untuk loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // State untuk refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Langsung sinkronkan data user dan ambil stats saat ViewModel dibuat
        syncUserProfile()
        fetchStats()
    }

    fun syncUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.syncUserProfile()
                .onSuccess {
                    // Setelah sync profile, fetch stats juga
                    fetchStats()
                }
                .onFailure {
                    _error.value = "Gagal sinkronisasi user: ${it.message}"
                }
            _isLoading.value = false
        }
    }

    fun fetchStats() {
        viewModelScope.launch {
            if (!_isLoading.value) {
                _isLoading.value = true
            }

            userRepository.getStats()
                .onSuccess { statsData ->
                    _stats.value = statsData
                    _error.value = null
                }
                .onFailure {
                    _error.value = "Gagal mengambil stats: ${it.message}"
                }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncUserProfile()
            fetchStats()
        }
    }

    fun clearError() {
        _error.value = null
    }
}