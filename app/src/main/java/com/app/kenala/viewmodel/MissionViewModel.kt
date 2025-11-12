package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.CompleteMissionRequest
import com.app.kenala.api.ResetProgressRequest
import com.app.kenala.api.RetrofitClient
import com.app.kenala.api.SkipClueRequest
import com.app.kenala.data.remote.dto.CheckLocationRequest
import com.app.kenala.data.remote.dto.CheckLocationResponse
import com.app.kenala.data.remote.dto.MissionDto
import com.app.kenala.data.remote.dto.MissionWithCluesResponse
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

    // --- STATE BARU UNTUK TRACKING ---
    // State untuk misi aktif (dengan clues)
    private val _missionWithClues = MutableStateFlow<MissionWithCluesResponse?>(null)
    val missionWithClues: StateFlow<MissionWithCluesResponse?> = _missionWithClues.asStateFlow()

    // State untuk response cek lokasi
    private val _checkLocationResponse = MutableStateFlow<CheckLocationResponse?>(null)
    val checkLocationResponse: StateFlow<CheckLocationResponse?> = _checkLocationResponse.asStateFlow()
    // ---------------------------------

    // State untuk loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- PERBAIKAN: Flag untuk "mengunci" state Selesai ---
    private var isMissionFinished = false
    // -----------------------------------------------------

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
                val response = apiService.getMissions(
                    category = if (category != "Acak") category else null,
                    budget = if (budget != "Acak") budget else null,
                    distance = if (distance != "Acak") distance else null
                )

                if (response.isSuccessful && response.body() != null) {
                    val missionList = response.body()!!
                    if (missionList.isNotEmpty()) {
                        // Select random mission
                        _selectedMission.value = missionList.random()
                        _missions.value = missionList // Simpan juga daftarnya
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

    // Get single mission by ID (TIDAK DIPAKAI LAGI, DIGANTI DENGAN DI BAWAH)
    fun getMission(id: String) {
        // ... (kode lama) ...
    }

    // --- FUNGSI BARU UNTUK TRACKING ---

    // Mengambil misi beserta semua petunjuknya
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

    // Mengecek lokasi user ke backend
    fun checkLocation(latitude: Double, longitude: Double) {
        // --- PERBAIKAN RACE CONDITION ---
        // Jika status sudah "tiba" (misalnya dari skip), JANGAN perbarui lagi dengan info GPS.
        if (isMissionFinished) {
            return
        }
        // ---------------------------------

        val missionId = _missionWithClues.value?.mission?.id
        if (missionId == null) {
            // Jangan set error jika misi belum dimuat
            return
        }

        viewModelScope.launch {
            // Tidak set _isLoading.value = true di sini agar UI tidak berkedip
            try {
                val request = CheckLocationRequest(missionId, latitude, longitude)
                val response = apiService.checkLocation(request)
                if (response.isSuccessful) {
                    val locationResponse = response.body()
                    _checkLocationResponse.value = locationResponse

                    // --- PERBAIKAN RACE CONDITION ---
                    // Jika response bilang kita sudah sampai, "kunci" statusnya
                    if (locationResponse?.destination?.isArrived == true) {
                        isMissionFinished = true
                    }
                    // ---------------------------------

                } else {
                    // Jangan set error jika hanya gagal cek lokasi, biarkan UI
                    println("Gagal cek lokasi: ${response.message()}")
                }
            } catch (e: Exception) {
                // Jangan set error jika hanya gagal cek lokasi
                println("Terjadi kesalahan koneksi: ${e.message}")
            }
        }
    }

    // --- TAMBAHAN BARU: Fungsi untuk Skip Clue ---
    fun skipCurrentClue() {
        val missionId = _missionWithClues.value?.mission?.id
        // Ambil ID clue saat ini dari response terakhir
        val clueId = _checkLocationResponse.value?.currentClue?.id

        if (missionId == null || clueId == null) {
            _error.value = "Tidak bisa melewati clue: data misi tidak lengkap."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Tampilkan loading saat skip
            _error.value = null
            try {
                val request = SkipClueRequest(mission_id = missionId, clue_id = clueId)
                val response = apiService.skipClue(request)

                if (response.isSuccessful) {
                    val skipResponse = response.body()
                    // Update UI dengan response baru (clue berikutnya atau status 'arrived')
                    _checkLocationResponse.value = skipResponse

                    // --- PERBAIKAN RACE CONDITION ---
                    // Jika response skip bilang kita sudah sampai, "kunci" statusnya
                    if (skipResponse?.destination?.isArrived == true) {
                        isMissionFinished = true
                    }
                    // ---------------------------------
                } else {
                    _error.value = "Gagal melewati clue: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            }
            _isLoading.value = false
        }
    }
    // -----------------------------------------

    // --- TAMBAHAN BARU: Fungsi untuk Reset Progress ---
    fun resetMissionProgress(missionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Reset state lokal dulu agar UI tidak menampilkan data lama
            _missionWithClues.value = null
            _checkLocationResponse.value = null
            isMissionFinished = false // Reset "kunci"

            try {
                val request = ResetProgressRequest(mission_id = missionId)
                apiService.resetMissionProgress(request)
                // Tidak peduli sukses atau gagal, kita tetap lanjut
            } catch (e: Exception) {
                // Error diabaikan, anggap saja progres sudah di-reset
                println("Gagal reset progress: ${e.message}")
            }
            // Panggil onComplete untuk memberi tahu UI agar lanjut fetch clues
            onComplete()
        }
    }
    // ----------------------------------------------

    // Complete mission
    fun completeMission(missionId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Menggunakan Request DTO yang baru
                val request = CompleteMissionRequest(mission_id = missionId)
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
        isMissionFinished = false // Reset "kunci"
    }

    fun clearError() {
        _error.value = null
    }
}