package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.remote.dto.MissionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MissionViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService

    // State untuk daftar missions
    private val _missions = MutableStateFlow<List<MissionDto>>(emptyList())
    val missions: StateFlow<List<MissionDto>> = _missions.asStateFlow()

    // State untuk selected mission (dari gacha)
    private val _selectedMission = MutableStateFlow<MissionDto?>(null)
    val selectedMission: StateFlow<MissionDto?> = _selectedMission.asStateFlow()

    // State untuk loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Fetch missions dengan filter
    fun fetchMissions(
        category: String? = null,
        budget: String? = null,
        distance: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = apiService.getMissions(
                    category = if (category != "Acak") category else null,
                    budget = if (budget != "Acak") budget else null,
                    distance = if (distance != "Acak") distance else null
                )

                if (response.isSuccessful && response.body() != null) {
                    _missions.value = response.body()!!
                } else {
                    _error.value = "Gagal mengambil misi: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    // Get random mission (untuk gacha)
    fun getRandomMission(
        category: String? = null,
        budget: String? = null,
        distance: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Fetch missions dengan filter
                fetchMissions(category, budget, distance)

                // Tunggu sampai missions loaded
                _missions.value.let { missionList ->
                    if (missionList.isNotEmpty()) {
                        // Select random mission
                        _selectedMission.value = missionList.random()
                    } else {
                        _error.value = "Tidak ada misi yang ditemukan"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    // Get single mission by ID
    fun getMission(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = apiService.getMission(id)

                if (response.isSuccessful && response.body() != null) {
                    _selectedMission.value = response.body()!!
                } else {
                    _error.value = "Gagal mengambil detail misi: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    // Complete mission
    fun completeMission(missionId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = apiService.completeMission(missionId)

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _error.value = "Gagal menyelesaikan misi: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    fun clearSelectedMission() {
        _selectedMission.value = null
    }

    fun clearError() {
        _error.value = null
    }
}