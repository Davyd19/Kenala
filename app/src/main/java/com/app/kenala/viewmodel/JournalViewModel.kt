package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.local.AppDatabase
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = JournalRepository(
        RetrofitClient.apiService,
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

    fun createJournal(title: String, story: String, imageUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createJournal(title, story, imageUrl)
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun updateJournal(id: String, title: String, story: String, imageUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateJournal(id, title, story, imageUrl)
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
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