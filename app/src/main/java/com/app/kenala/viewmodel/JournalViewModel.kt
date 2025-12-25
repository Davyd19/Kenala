package com.app.kenala.viewmodel

import android.app.Application
import android.net.Uri
import android.webkit.MimeTypeMap
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

                val imageFile = imageUri?.let { uriToFile(it) }

                repository.createJournal(userId, title, story, imageFile, locationName, latitude, longitude)
                    .onSuccess {
                        _journalSaved.value = true
                        syncJournals()
                    }
                    .onFailure {
                        _error.value = it.message
                    }

                imageFile?.delete()

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
                val imageFile = imageUri?.let { uriToFile(it) }

                repository.updateJournal(id, title, story, imageFile)
                    .onSuccess {
                        _journalSaved.value = true
                        syncJournals()
                    }
                    .onFailure {
                        _error.value = it.message
                    }

                imageFile?.delete()

            } catch (e: Exception) {
                _error.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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

    private fun uriToFile(uri: Uri): File? {
        val context = getApplication<Application>().applicationContext
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            val tempFile = File.createTempFile("journal_${System.currentTimeMillis()}", ".$extension", context.cacheDir)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}