package com.app.kenala.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.UserEntity
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.data.remote.dto.StreakDto
import com.app.kenala.data.remote.dto.WeeklyChallengeDto
import com.app.kenala.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val apiService = RetrofitClient.apiService
    private val userRepository = UserRepository(
        apiService,
        database.userDao()
    )

    val user: StateFlow<UserEntity?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _stats = MutableStateFlow<StatsDto?>(null)
    val stats: StateFlow<StatsDto?> = _stats.asStateFlow()

    private val _streakData = MutableStateFlow<StreakDto?>(null)
    val streakData: StateFlow<StreakDto?> = _streakData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _weeklyChallenge = MutableStateFlow<WeeklyChallengeDto?>(null)
    val weeklyChallenge: StateFlow<WeeklyChallengeDto?> = _weeklyChallenge.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val jobs = listOf(
                launch { syncUserProfile() },
                launch { fetchStats() },
                launch { fetchStreak() }
            )
            jobs.joinAll()
            _isRefreshing.value = false
        }
    }

    fun fetchStreak() {
        viewModelScope.launch {
            userRepository.getStreak()
                .onSuccess { data -> _streakData.value = data }
                .onFailure { if (!_isRefreshing.value) _error.value = it.message }
        }
    }

    fun syncUserProfile() {
        viewModelScope.launch {
            userRepository.syncUserProfile()
                .onFailure { if (!_isRefreshing.value) _error.value = it.message }
        }
    }

    fun fetchStats() {
        viewModelScope.launch {
            userRepository.getStats()
                .onSuccess { statsData -> _stats.value = statsData }
                .onFailure { if (!_isRefreshing.value) _error.value = it.message }
        }
    }

    fun updateProfile(
        name: String,
        phone: String?,
        bio: String?,
        imageUri: Uri?,
        existingImageUrl: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val imageUrl = if (imageUri != null) {
                    val uploadResult = uploadImage(imageUri)
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()
                    } else {
                        _error.value = "Gagal upload gambar: ${uploadResult.exceptionOrNull()?.message}"
                        _isLoading.value = false
                        return@launch
                    }
                } else {
                    existingImageUrl
                }

                userRepository.updateProfile(name, phone, bio, imageUrl)
                    .onSuccess {
                        _isLoading.value = false
                        onSuccess()
                        refresh()
                    }
                    .onFailure {
                        _error.value = "Gagal update profil: ${it.message}"
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): Result<String?> {
        return try {
            val context = getApplication<Application>().applicationContext
            val contentResolver = context.contentResolver

            var fileName: String? = null
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
            fileName = fileName ?: "image_${System.currentTimeMillis()}"

            val inputStream = contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, fileName!!)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = file.asRequestBody(
                contentResolver.getType(uri)?.toMediaTypeOrNull()
            )

            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = apiService.uploadImage(body)

            file.delete()

            if (response.isSuccessful) {
                val url = response.body()?.get("url") ?: response.body()?.get("imageUrl")
                Result.success(url)
            } else {
                Result.failure(Exception("Upload gagal: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearError() {
        _error.value = null
    }
}