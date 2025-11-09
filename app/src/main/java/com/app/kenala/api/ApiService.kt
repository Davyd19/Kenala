package com.app.kenala.api

import com.app.kenala.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Journal endpoints
    @GET("journals")
    suspend fun getJournals(): Response<List<JournalDto>>

    @GET("journals/{id}")
    suspend fun getJournal(@Path("id") id: String): Response<JournalDto>

    @POST("journals")
    suspend fun createJournal(@Body journal: CreateJournalRequest): Response<JournalDto>

    @PUT("journals/{id}")
    suspend fun updateJournal(
        @Path("id") id: String,
        @Body journal: UpdateJournalRequest
    ): Response<JournalDto>

    @DELETE("journals/{id}")
    suspend fun deleteJournal(@Path("id") id: String): Response<Unit>

    // Mission endpoints
    @GET("missions")
    suspend fun getMissions(
        @Query("category") category: String? = null,
        @Query("budget") budget: String? = null,
        @Query("distance") distance: String? = null
    ): Response<List<MissionDto>>

    @GET("missions/{id}")
    suspend fun getMission(@Path("id") id: String): Response<MissionDto>

    @POST("missions/{id}/complete")
    suspend fun completeMission(@Path("id") id: String): Response<Unit>

    // Profile endpoints
    @GET("profile")
    suspend fun getProfile(): Response<UserDto>

    @PUT("profile")
    suspend fun updateProfile(@Body profile: UpdateProfileRequest): Response<UserDto>

    @GET("profile/stats")
    suspend fun getStats(): Response<StatsDto>

    @GET("profile/badges")
    suspend fun getBadges(): Response<List<BadgeDto>>
}

// Request/Response DTOs
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class CreateJournalRequest(
    val title: String,
    val story: String,
    val imageUrl: String?,
    val location: LocationDto?
)

data class UpdateJournalRequest(
    val title: String,
    val story: String,
    val imageUrl: String?
)

data class UpdateProfileRequest(
    val name: String?,
    val phone: String?,
    val bio: String?,
    val profile_image_url: String?
)