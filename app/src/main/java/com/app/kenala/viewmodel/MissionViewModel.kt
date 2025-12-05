package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.remote.dto.* // Import semua DTO dari sini
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MissionViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService

    private val _missions = MutableStateFlow<List<MissionDto>>(emptyList())
    val missions: StateFlow<List<MissionDto>> = _missions.asStateFlow()

    private val _selectedMission = MutableStateFlow<MissionDto?>(null)
    val selectedMission: StateFlow<MissionDto?> = _selectedMission.asStateFlow()

    private val _missionWithClues = MutableStateFlow<MissionWithCluesResponse?>(null)
    val missionWithClues: StateFlow<MissionWithCluesResponse?> = _missionWithClues.asStateFlow()

    private val _checkLocationResponse = MutableStateFlow<CheckLocationResponse?>(null)
    val checkLocationResponse: StateFlow<CheckLocationResponse?> = _checkLocationResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var isMissionFinished = false

    fun fetchMissions(category: String? = null, budget: String? = null, distance: String? = null) {
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

    fun getRandomMission(category: String? = null, budget: String? = null, distance: String? = null) {
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
                    val missionList = response.body()!!
                    if (missionList.isNotEmpty()) {
                        _selectedMission.value = missionList.random()
                        _missions.value = missionList
                    } else {
                        _error.value = "Tidak ada misi yang ditemukan"
                    }
                } else {
                    _error.value = "Gagal mengambil misi acak: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun fetchMissionWithClues(missionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getMissionWithClues(missionId)
                if (response.isSuccessful) {
                    _missionWithClues.value = response.body()
                } else {
                    _error.value = "Gagal mengambil detail misi & clues: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun checkLocation(latitude: Double, longitude: Double) {
        if (isMissionFinished) return

        val missionId = _missionWithClues.value?.mission?.id ?: return

        viewModelScope.launch {
            try {
                val request = CheckLocationRequest(missionId, latitude, longitude)
                val response = apiService.checkLocation(request)

                if (response.isSuccessful) {
                    val locationResponse = response.body()
                    _checkLocationResponse.value = locationResponse
                    if (locationResponse?.destination?.isArrived == true) {
                        isMissionFinished = true
                    }
                } else {
                    println("Gagal cek lokasi: ${response.message()}")
                }
            } catch (e: Exception) {
                println("Terjadi kesalahan koneksi: ${e.message}")
            }
        }
    }

    fun skipCurrentClue() {
        val missionId = _missionWithClues.value?.mission?.id
        val clueId = _checkLocationResponse.value?.currentClue?.id

        if (missionId == null || clueId == null) {
            _error.value = "Tidak bisa melewati clue: data misi tidak lengkap."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val request = SkipClueRequest(missionId = missionId, clueId = clueId)
                val response = apiService.skipClue(request)

                if (response.isSuccessful) {
                    val skipResponse = response.body()
                    _checkLocationResponse.value = skipResponse
                    if (skipResponse?.destination?.isArrived == true) {
                        isMissionFinished = true
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    if (errorMsg.contains("Clue terakhir")) {
                        _error.value = "Clue terakhir tidak bisa dilewati!"
                    } else {
                        _error.value = "Gagal melewati clue: $errorMsg"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun resetMissionProgress(missionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _missionWithClues.value = null
            _checkLocationResponse.value = null
            isMissionFinished = false
            try {
                val request = ResetProgressRequest(missionId = missionId)
                apiService.resetMissionProgress(request)
            } catch (e: Exception) {
                println("Gagal reset progress: ${e.message}")
            }
            onComplete()
        }
    }

    fun completeMission(missionId: String, realDistanceMeters: Double = 0.0, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = CompleteMissionRequest(
                    missionId = missionId,
                    realDistanceMeters = realDistanceMeters
                )
                val response = apiService.completeMission(request)

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
        _missionWithClues.value = null
        _checkLocationResponse.value = null
        isMissionFinished = false
    }

    fun clearError() {
        _error.value = null
    }
}