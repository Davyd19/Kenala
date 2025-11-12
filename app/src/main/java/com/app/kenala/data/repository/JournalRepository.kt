package com.app.kenala.data.repository

import com.app.kenala.api.ApiService
import com.app.kenala.api.CreateJournalRequest
import com.app.kenala.api.UpdateJournalRequest
import com.app.kenala.data.local.dao.JournalDao
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.remote.dto.JournalDto
import com.app.kenala.data.remote.dto.LocationDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class JournalRepository(
    private val apiService: ApiService,
    private val journalDao: JournalDao
) {

    // Get journals with offline-first approach
    fun getJournals(): Flow<List<JournalEntity>> {
        return journalDao.getAllJournals()
    }

    // Sync journals from server
    suspend fun syncJournals(): Result<Unit> {
        return try {
            val response = apiService.getJournals()
            if (response.isSuccessful) {
                response.body()?.let { journals ->
                    val entities = journals.map { it.toEntity() }
                    journalDao.insertJournals(entities)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create journal (try server first, fallback to local)
    suspend fun createJournal(
        title: String,
        story: String,
        imageUrl: String?,
        locationName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<JournalEntity> {
        return try {
            val location = if (locationName != null && latitude != null && longitude != null) {
                LocationDto(locationName, latitude, longitude)
            } else null

            val request = CreateJournalRequest(title, story, imageUrl, location)
            val response = apiService.createJournal(request)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val entity = dto.toEntity()
                    journalDao.insertJournal(entity)
                    Result.success(entity)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                // Save locally if server fails
                val localEntity = JournalEntity(
                    id = "local_${System.currentTimeMillis()}",
                    title = title,
                    story = story,
                    date = System.currentTimeMillis().toString(),
                    imageUrl = imageUrl,
                    locationName = locationName,
                    latitude = latitude,
                    longitude = longitude,
                    isSynced = false
                )
                journalDao.insertJournal(localEntity)
                Result.success(localEntity)
            }
        } catch (e: Exception) {
            // Network error - save locally
            val localEntity = JournalEntity(
                id = "local_${System.currentTimeMillis()}",
                title = title,
                story = story,
                date = System.currentTimeMillis().toString(),
                imageUrl = imageUrl,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                isSynced = false
            )
            journalDao.insertJournal(localEntity)
            Result.success(localEntity)
        }
    }

    // Update journal
    suspend fun updateJournal(
        id: String,
        title: String,
        story: String,
        imageUrl: String?
    ): Result<Unit> {
        return try {
            val request = UpdateJournalRequest(title, story, imageUrl)
            val response = apiService.updateJournal(id, request)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    journalDao.updateJournal(dto.toEntity())
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Update failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete journal
    suspend fun deleteJournal(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteJournal(id)
            if (response.isSuccessful) {
                journalDao.deleteJournalById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert DTO to Entity
fun JournalDto.toEntity() = JournalEntity(
    id = this.id,
    title = this.title,
    story = this.story,
    date = this.date,
    imageUrl = this.image_url,
    locationName = this.location?.name,
    latitude = this.location?.latitude,
    longitude = this.location?.longitude,
    isSynced = true
)