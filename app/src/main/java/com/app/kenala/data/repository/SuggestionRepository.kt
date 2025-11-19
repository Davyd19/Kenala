package com.app.kenala.data.repository

import com.app.kenala.api.ApiService
import com.app.kenala.data.remote.dto.CreateSuggestionRequest
import com.app.kenala.data.remote.dto.SuggestionDto
import com.app.kenala.data.remote.dto.UpdateSuggestionRequest

class SuggestionRepository(private val apiService: ApiService) {

    suspend fun getSuggestions(): Result<List<SuggestionDto>> {
        return try {
            val response = apiService.getSuggestions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Gagal mengambil saran: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSuggestion(
        locationName: String,
        address: String,
        category: String,
        description: String
    ): Result<SuggestionDto> {
        return try {
            val request = CreateSuggestionRequest(locationName, address, category, description)
            val response = apiService.createSuggestion(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal membuat saran: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSuggestion(
        id: String,
        locationName: String,
        address: String,
        category: String,
        description: String
    ): Result<SuggestionDto> {
        return try {
            val request = UpdateSuggestionRequest(locationName, address, category, description)
            val response = apiService.updateSuggestion(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memperbarui saran: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSuggestion(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteSuggestion(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menghapus saran: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}