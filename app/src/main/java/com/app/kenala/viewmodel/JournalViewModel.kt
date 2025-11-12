package com.app.kenala.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.api.CreateJournalRequest
import com.app.kenala.api.UpdateJournalRequest
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.remote.dto.LocationDto
import com.app.kenala.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody // <-- 1. IMPORT DIPERBAIKI
import java.io.File
import java.io.FileOutputStream

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val apiService = RetrofitClient.apiService
    private val repository = JournalRepository(
        apiService,
        database.journalDao()
    )

    // State untuk journals
    val journals: StateFlow<List<JournalEntity>> = repository.getJournals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // State untuk loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk error
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

    // --- FUNGSI DIPERBARUI: Menerima imageUri ---
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
                // 1. Upload gambar terlebih dahulu jika ada
                val imageUrl = if (imageUri != null) {
                    // --- 2. PERBAIKAN: Menggunakan if/else, bukan when ---
                    val uploadResult = uploadImage(imageUri)
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()
                    } else {
                        _error.value = "Gagal upload gambar: ${uploadResult.exceptionOrNull()?.message}"
                        _isLoading.value = false
                        return@launch
                    }
                    // --------------------------------------------------
                } else {
                    null
                }

                // 2. Buat jurnal dengan URL gambar yang didapat
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

    // --- FUNGSI DIPERBARUI: Menerima imageUri ---
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
                // 1. Upload gambar HANYA jika URI baru dipilih
                val imageUrl = if (imageUri != null) {
                    // --- 2. PERBAIKAN: Menggunakan if/else, bukan when ---
                    val uploadResult = uploadImage(imageUri)
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()
                    } else {
                        _error.value = "Gagal upload gambar: ${uploadResult.exceptionOrNull()?.message}"
                        _isLoading.value = false
                        return@launch
                    }
                    // --------------------------------------------------
                } else {
                    existingImageUrl // Gunakan URL yang lama
                }

                // 2. Update jurnal dengan URL gambar yang didapat
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

    // --- FUNGSI BARU: Untuk upload gambar ---
    private suspend fun uploadImage(uri: Uri): Result<String?> {
        return try {
            val context = getApplication<Application>().applicationContext
            val contentResolver = context.contentResolver

            // 1. Dapatkan nama file asli
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

            // 2. Salin file dari content URI ke file cache lokal
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, fileName!!)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // 3. Buat RequestBody dari file cache
            // --- 3. PERBAIKAN: Menggunakan asRequestBody ---
            val requestFile = file.asRequestBody(
                contentResolver.getType(uri)?.toMediaTypeOrNull()
            )
            // ------------------------------------------

            // 4. Buat MultipartBody.Part
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // 5. Panggil API
            val response = apiService.uploadImage(body)

            // 6. Hapus file cache
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