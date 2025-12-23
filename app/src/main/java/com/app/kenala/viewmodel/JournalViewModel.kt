package com.app.kenala.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.repository.JournalRepository
import com.app.kenala.utils.DataStoreManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val apiService = RetrofitClient.apiService
    private val dataStoreManager = DataStoreManager(application)

    private val repository = JournalRepository(
        apiService,
        database.journalDao(),
        application.applicationContext
    )

    private val currentUserId: Flow<String?> = dataStoreManager.userId

    val journals: StateFlow<List<JournalEntity>> = currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                repository.getJournals(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _journalSaved = MutableStateFlow(false)
    val journalSaved: StateFlow<Boolean> = _journalSaved.asStateFlow()

    init {
        syncJournals()
    }

    fun resetJournalSaved() {
        _journalSaved.value = false
    }

    fun syncJournals() {
        viewModelScope.launch {
            val userId = currentUserId.first()
            if (userId != null) {
                repository.syncJournals(userId)
                    .onFailure {
                        Log.e("JournalViewModel", "Sync failed: ${it.message}")
                    }
            }
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
            _journalSaved.value = false

            try {
                val userId = currentUserId.first()
                if (userId == null) {
                    _error.value = "User tidak terautentikasi"
                    _isLoading.value = false
                    return@launch
                }

                var finalImageUrl: String? = null

                if (imageUri != null) {
                    val uploadResult = uploadImage(imageUri)
                    if (uploadResult.isSuccess) {
                        finalImageUrl = uploadResult.getOrNull()
                    } else {
                        _error.value = "Gagal upload gambar: ${uploadResult.exceptionOrNull()?.message}"
                        _isLoading.value = false
                        return@launch
                    }
                }

                repository.createJournal(userId, title, story, finalImageUrl, locationName, latitude, longitude)
                    .onSuccess {
                        _journalSaved.value = true
                        syncJournals()
                    }
                    .onFailure {
                        _error.value = it.message
                    }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            } finally {
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
            _journalSaved.value = false

            try {
                val finalImageUrl = if (imageUri != null) {
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

                repository.updateJournal(id, title, story, finalImageUrl)
                    .onSuccess {
                        _journalSaved.value = true
                        syncJournals()
                    }
                    .onFailure {
                        _error.value = it.message
                    }
            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): Result<String?> {
        return try {
            val context = getApplication<Application>().applicationContext
            val contentResolver = context.contentResolver

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            val fileName = "upload_${System.currentTimeMillis()}.$extension"

            val file = File(context.cacheDir, fileName)
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())

            // 3. Upload dengan key "image"
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = apiService.uploadImage(body)
            file.delete()

            if (response.isSuccessful) {
                val responseBody = response.body()

                var url = responseBody?.get("url")
                    ?: responseBody?.get("imageUrl")
                    ?: responseBody?.get("path")
                    ?: responseBody?.get("file")
                    ?: responseBody?.get("data")

                if (!url.isNullOrEmpty()) {
                    url = url.replace("\\", "/")

                    Log.d("JournalViewModel", "Gambar berhasil diupload, URL bersih: $url")
                    Result.success(url)
                } else {
                    Result.failure(Exception("Server sukses, tapi URL gambar kosong."))
                }
            } else {
                val errorMsg = "Upload gagal: ${response.code()} ${response.message()}"
                Log.e("JournalViewModel", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("JournalViewModel", "Error upload", e)
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