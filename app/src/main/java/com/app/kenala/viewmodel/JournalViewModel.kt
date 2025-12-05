package com.app.kenala.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.remote.dto.CreateJournalRequest // IMPORT DTO
import com.app.kenala.data.remote.dto.UpdateJournalRequest // IMPORT DTO
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class JournalViewModel(application: Application) : AndroidViewModel(application) {
    // ... (Isi class tetap sama, hanya import di atas yang berubah)
    // Saya tulis ulang bagian atas class untuk konteks

    private val database = AppDatabase.getDatabase(application)
    private val apiService = RetrofitClient.apiService
    private val repository = JournalRepository(
        apiService,
        database.journalDao()
    )

    val journals: StateFlow<List<JournalEntity>> = repository.getJournals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        syncJournals()
    }

    fun syncJournals() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncJournals()
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun createJournal(
        title: String,
        story: String,
        imageUri: Uri?,
        locationName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
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
                    null
                }

                repository.createJournal(title, story, imageUrl, locationName, latitude, longitude)
                    .onFailure {
                        _error.value = it.message
                    }
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateJournal(
        id: String,
        title: String,
        story: String,
        imageUri: Uri?,
        existingImageUrl: String?
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

                repository.updateJournal(id, title, story, imageUrl)
                    .onFailure {
                        _error.value = it.message
                    }
                _isLoading.value = false

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
                Result.success(response.body()?.imageUrl)
            } else {
                Result.failure(Exception("Upload gagal: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteJournal(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteJournal(id)
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}