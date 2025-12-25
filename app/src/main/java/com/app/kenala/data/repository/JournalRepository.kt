package com.app.kenala.data.repository

import android.content.Context
import com.app.kenala.api.ApiService
import com.app.kenala.data.local.dao.JournalDao
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.remote.dto.JournalDto
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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
                Result.failure(Exception("Gagal sinkronisasi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createJournal(
        userId: String,
        title: String,
        story: String,
        imageFile: File?,
        locationName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<JournalEntity> {

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val storyPart = story.toRequestBody("text/plain".toMediaTypeOrNull())

        val locationNamePart = locationName?.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudePart = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudePart = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val missionIdPart = null

        val imagePart = imageFile?.let { file ->
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        return try {
            val response = apiService.createJournal(
                title = titlePart,
                story = storyPart,
                locationName = locationNamePart,
                latitude = latitudePart,
                longitude = longitudePart,
                missionId = missionIdPart,
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                val entity = response.body()!!.toEntity()
                journalDao.insertJournal(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("Gagal membuat jurnal: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJournal(
        id: String,
        title: String,
        story: String,
        imageFile: File?
    ): Result<Unit> {

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val storyPart = story.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let { file ->
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        return try {
            val response = apiService.updateJournal(id, titlePart, storyPart, imagePart)

            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    journalDao.updateJournal(dto.toEntity())
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal update jurnal: ${response.message()}"))
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