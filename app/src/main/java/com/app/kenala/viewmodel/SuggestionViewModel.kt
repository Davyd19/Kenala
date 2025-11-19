package com.app.kenala.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.kenala.api.RetrofitClient
import com.app.kenala.data.remote.dto.SuggestionDto
import com.app.kenala.data.repository.SuggestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val repository = SuggestionRepository(apiService)

    private val _suggestions = MutableStateFlow<List<SuggestionDto>>(emptyList())
    val suggestions: StateFlow<List<SuggestionDto>> = _suggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchSuggestions()
    }

    fun fetchSuggestions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getSuggestions()
                .onSuccess {
                    _suggestions.value = it
                }
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun addSuggestion(
        locationName: String,
        address: String,
        category: String,
        description: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.createSuggestion(locationName, address, category, description)
                .onSuccess { newSuggestion ->
                    _suggestions.value = listOf(newSuggestion) + _suggestions.value
                    onComplete()
                }
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun updateSuggestion(
        id: String,
        locationName: String,
        address: String,
        category: String,
        description: String,
        onComplete: (SuggestionDto) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.updateSuggestion(id, locationName, address, category, description)
                .onSuccess { updatedSuggestion ->
                    // Ganti item lama di list dengan item baru
                    _suggestions.value = _suggestions.value.map {
                        if (it.id == updatedSuggestion.id) updatedSuggestion else it
                    }
                    onComplete(updatedSuggestion)
                }
                .onFailure {
                    _error.value = it.message
                }
            _isLoading.value = false
        }
    }

    fun deleteSuggestion(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.deleteSuggestion(id)
                .onSuccess {
                    _suggestions.value = _suggestions.value.filter { it.id != id }
                    onComplete()
                }
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