package com.app.kenala.data.repository

import android.content.Context
import com.app.kenala.api.ApiService
import com.app.kenala.data.local.dao.JournalDao
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.remote.dto.CreateJournalRequest
import com.app.kenala.data.remote.dto.JournalDto
import com.app.kenala.data.remote.dto.LocationDto
import com.app.kenala.data.remote.dto.UpdateJournalRequest
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val apiService: ApiService,
    private val journalDao: JournalDao,
    private val context: Context
) {

    fun getJournals(userId: String): Flow<List<JournalEntity>> {
        return journalDao.getAllJournals(userId)
    }

    suspend fun getJournalById(id: String): JournalEntity? {
        return journalDao.getJournalById(id)
    }

    suspend fun syncJournals(userId: String): Result<Unit> {
        return try {
            val response = apiService.getJournals()
            if (response.isSuccessful) {
                response.body()?.let { journalsDto ->
                    val userJournals = journalsDto.filter { it.user_id == userId }
                    val entities = userJournals.map { it.toEntity() }
                    journalDao.insertJournals(entities)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal sinkronisasi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createJournal(
        userId: String,
        title: String,
        story: String,
        imageUrl: String?,
        locationName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<JournalEntity> {
        val location = if (locationName != null && latitude != null && longitude != null) {
            LocationDto(locationName, latitude, longitude)
        } else null

        val request = CreateJournalRequest(title, story, imageUrl, location)

        return try {
            val response = apiService.createJournal(request)
            if (response.isSuccessful && response.body() != null) {
                val entity = response.body()!!.toEntity()
                journalDao.insertJournal(entity)
                Result.success(entity)
            } else {
                val localEntity = createLocalEntity(userId, title, story, imageUrl, locationName, latitude, longitude)
                journalDao.insertJournal(localEntity)
                Result.success(localEntity)
            }
        } catch (e: Exception) {
            val localEntity = createLocalEntity(userId, title, story, imageUrl, locationName, latitude, longitude)
            journalDao.insertJournal(localEntity)
            Result.success(localEntity)
        }
    }

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
                Result.failure(Exception("Gagal update jurnal: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteJournal(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteJournal(id)
            if (response.isSuccessful) {
                journalDao.deleteJournalById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal menghapus jurnal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createLocalEntity(
        userId: String,
        title: String,
        story: String,
        imageUrl: String?,
        locationName: String?,
        latitude: Double?,
        longitude: Double?
    ) = JournalEntity(
        id = "local_${System.currentTimeMillis()}",
        userId = userId,
        title = title,
        story = story,
        date = System.currentTimeMillis().toString(),
        imageUrl = imageUrl,
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        isSynced = false
    )

    private fun JournalDto.toEntity() = JournalEntity(
        id = this.id,
        userId = this.user_id,
        title = this.title,
        story = this.story,
        date = this.date,
        imageUrl = this.image_url,
        locationName = this.location?.name ?: this.location_name,
        latitude = this.location?.latitude ?: this.latitude,
        longitude = this.location?.longitude ?: this.longitude,
        isSynced = true
    )
}