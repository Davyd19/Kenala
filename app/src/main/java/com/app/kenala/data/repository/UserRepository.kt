package com.app.kenala.data.repository

import com.app.kenala.api.ApiService
import com.app.kenala.data.remote.dto.UpdateProfileRequest // IMPORT DTO
import com.app.kenala.data.local.dao.UserDao
import com.app.kenala.data.local.entities.UserEntity
import com.app.kenala.data.remote.dto.StatsDto
import com.app.kenala.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow
import com.app.kenala.data.remote.dto.StreakDto

class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {

    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun syncUserProfile(): Result<Unit> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                userDao.insertUser(response.body()!!.toEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal mengambil profil: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<StatsDto> {
        return try {
            val response = apiService.getStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal mengambil statistik: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        name: String,
        phone: String?,
        bio: String?,
        profileImageUrl: String?
    ): Result<Unit> {
        return try {
            val request = UpdateProfileRequest(
                name = name,
                phone = phone,
                bio = bio,
                profile_image_url = profileImageUrl
            )
            val response = apiService.updateProfile(request)

            if (response.isSuccessful && response.body() != null) {
                userDao.insertUser(response.body()!!.toEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gagal update profil: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStreak(): Result<StreakDto> {
        return try {
            val response = apiService.getStreak()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal mengambil data streak: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun UserDto.toEntity() = UserEntity(
    id = this.id,
    name = this.name,
    email = this.email,
    phone = this.phone,
    bio = this.bio,
    profile_image_url = this.profile_image_url,
    total_missions = this.total_missions,
    total_distance = this.total_distance,
    current_streak = this.current_streak,
    longest_streak = this.longest_streak,
    total_active_days = this.total_active_days
)