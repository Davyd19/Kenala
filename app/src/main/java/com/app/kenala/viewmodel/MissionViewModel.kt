package com.app.kenala.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.remote.dto.*
import com.app.kenala.utils.SocketManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

// Event khusus untuk notifikasi dan navigasi setelah misi selesai
sealed class MissionEvent {
    data class ShowNotification(val title: String, val message: String) : MissionEvent()
    object MissionCompletedSuccessfully : MissionEvent()
}

class MissionViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService

    // State untuk daftar misi
    private val _missions = MutableStateFlow<List<MissionDto>>(emptyList())
    val missions: StateFlow<List<MissionDto>> = _missions.asStateFlow()

    // State untuk misi yang sedang dipilih
    private val _selectedMission = MutableStateFlow<MissionDto?>(null)
    val selectedMission: StateFlow<MissionDto?> = _selectedMission.asStateFlow()

    // State untuk detail misi dan clue-clue nya
    private val _missionWithClues = MutableStateFlow<MissionWithCluesResponse?>(null)
    val missionWithClues: StateFlow<MissionWithCluesResponse?> = _missionWithClues.asStateFlow()

    // State untuk hasil tracking lokasi terbaru
    private val _checkLocationResponse = MutableStateFlow<CheckLocationResponse?>(null)
    val checkLocationResponse: StateFlow<CheckLocationResponse?> = _checkLocationResponse.asStateFlow()

    // Flow untuk event satu kali (notifikasi/toast)
    private val _missionEvent = MutableSharedFlow<MissionEvent>()
    val missionEvent: SharedFlow<MissionEvent> = _missionEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // State tracking
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // Guard agar API complete tidak terpanggil berkali-kali dalam satu sesi
    private var isMissionFinished = false

    /**
     * Mengambil daftar misi berdasarkan kategori, budget, dan jarak
     */
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

    /**
     * Mengambil misi secara acak (Gacha)
     */
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

    /**
     * Mengambil detail misi beserta list clues
     */
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

    /**
     * Logika Cek Lokasi: Dipanggil otomatis oleh GPS
     */
    fun checkLocation(latitude: Double, longitude: Double) {
        if (isMissionFinished) return

        val missionId = _missionWithClues.value?.mission?.id ?: return

        viewModelScope.launch {
            try {
                val request = CheckLocationRequest(missionId, latitude, longitude)
                val response = apiService.checkLocation(request)

                if (response.isSuccessful) {
                    val locationResponse = response.body()
                    val previousStatus = _checkLocationResponse.value?.status
                    val isArrived = locationResponse?.destination?.isArrived == true
                    val clueReached = locationResponse?.clueReached == true

                    _checkLocationResponse.value = locationResponse

                    // 1. Notifikasi jika sampai di clue
                    if (clueReached && previousStatus != "clue_reached") {
                        _missionEvent.emit(MissionEvent.ShowNotification(
                            "Petunjuk Ditemukan!",
                            "Anda telah mencapai lokasi petunjuk."
                        ))
                    }

                    // 2. TRIGGER OTOMATIS: Jika sampai di tujuan akhir misi
                    else if (isArrived && !isMissionFinished) {
                        isMissionFinished = true

                        _missionEvent.emit(MissionEvent.ShowNotification(
                            "Tiba di Tujuan!",
                            "Selamat! Menyimpan hasil misi Anda..."
                        ))

                        // Panggil API complete agar Statistik & Badge tercatat di Database
                        val dist = locationResponse?.destination?.distance ?: 0.0
                        autoCompleteMissionOnArrival(missionId, dist)
                    }

                }
            } catch (e: Exception) {
                println("Koneksi terganggu: ${e.message}")
            }
        }
    }

    /**
     * Fungsi internal untuk sinkronisasi data ke server saat misi selesai
     */
    private fun autoCompleteMissionOnArrival(missionId: String, distance: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Pastikan parameter ini SAMA dengan yang didefinisikan di CompleteMissionRequest
                val request = CompleteMissionRequest(
                    missionId = missionId,
                    realDistanceMeters = distance
                )
                val response = apiService.completeMission(request)

                if (response.isSuccessful) {
                    // Berhasil catat, emit event sukses
                    _missionEvent.emit(MissionEvent.MissionCompletedSuccessfully)
                } else {
                    // Cek detail error dari server
                    val errorBody = response.errorBody()?.string() ?: ""

                    // Jika backend menolak karena duplikasi, anggap sukses di UI agar user tidak bingung
                    if (errorBody.contains("already completed", ignoreCase = true)) {
                        _missionEvent.emit(MissionEvent.MissionCompletedSuccessfully)
                    } else {
                        _error.value = "Server gagal mencatat: ${response.message()}"
                        isMissionFinished = false // Reset lock agar bisa coba lagi
                    }
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan koneksi saat menyimpan hasil misi."
                isMissionFinished = false
            }
            _isLoading.value = false
        }
    }

    /**
     * Melewati clue saat ini secara manual
     */
    fun skipCurrentClue() {
        val missionId = _missionWithClues.value?.mission?.id
        val clueId = _checkLocationResponse.value?.currentClue?.id

        if (missionId == null || clueId == null) {
            _error.value = "Data tidak lengkap untuk melewati clue."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = SkipClueRequest(missionId = missionId, clueId = clueId)
                val response = apiService.skipClue(request)

                if (response.isSuccessful) {
                    val skipResponse = response.body()
                    _checkLocationResponse.value = skipResponse

                    if (skipResponse?.destination?.isArrived == true) {
                        // Jika skip clue terakhir membawa ke tujuan akhir
                        checkLocation(0.0, 0.0)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    _error.value = if (errorMsg.contains("Clue terakhir")) {
                        "Clue terakhir tidak bisa dilewati!"
                    } else {
                        "Gagal melewati clue."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Kesalahan koneksi: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    /**
     * Menghapus progress misi saat ini (Reset)
     */
    fun resetMissionProgress(missionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _missionWithClues.value = null
            _checkLocationResponse.value = null
            isMissionFinished = false
            try {
                val request = ResetProgressRequest(missionId = missionId)
                apiService.resetMissionProgress(request)
            } catch (e: Exception) {
                println("Gagal reset: ${e.message}")
            }
            onComplete()
        }
    }

    /**
     * Fungsi manual untuk menyelesaikan misi (jika dibutuhkan tombol manual)
     */
    fun completeMission(missionId: String, realDistanceMeters: Double = 0.0, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // PERBAIKAN: Gunakan parameter CamelCase
                val request = CompleteMissionRequest(
                    missionId = missionId,
                    realDistanceMeters = realDistanceMeters
                )
                val response = apiService.completeMission(request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _error.value = "Gagal menyimpan misi."
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    // --- INTEGRASI REAL-TIME TRACKING (SOCKET.IO) ---
    // Dipanggil dari GuidanceScreen saat masuk
    fun startRealtimeTracking(missionId: String, userId: String) {
        viewModelScope.launch {
            _isTracking.value = true
            try {
                SocketManager.connect()
                val joinData = JSONObject().apply {
                    put("missionId", missionId)
                    put("userId", userId)
                }
                SocketManager.emit("join_mission", joinData)
                Log.d("MissionViewModel", "Socket join_mission emitted for mission $missionId")
            } catch (e: Exception) {
                Log.e("MissionViewModel", "Socket error: ${e.message}")
            }
        }
    }

    fun stopRealtimeTracking(missionId: String, userId: String) {
        viewModelScope.launch {
            _isTracking.value = false
            try {
                val leaveData = JSONObject().apply {
                    put("missionId", missionId)
                    put("userId", userId)
                }
                SocketManager.emit("leave_mission", leaveData)
                Log.d("MissionViewModel", "Socket leave_mission emitted")
                // Opsional: SocketManager.disconnect() jika ingin memutus koneksi sepenuhnya
            } catch (e: Exception) {
                Log.e("MissionViewModel", "Socket error: ${e.message}")
            }
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