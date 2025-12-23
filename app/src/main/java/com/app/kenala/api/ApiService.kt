package com.app.kenala.api

import com.app.kenala.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<GeneralResponse>

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

    @GET("missions")
    suspend fun getMissions(
        @Query("category") category: String? = null,
        @Query("budget") budget: String? = null,
        @Query("distance") distance: String? = null
    ): Response<List<MissionDto>>

    @GET("missions/{id}")
    suspend fun getMission(@Path("id") id: String): Response<MissionDto>

    @POST("missions/complete")
    suspend fun completeMission(@Body request: CompleteMissionRequest): Response<Unit>

    @GET("profile")
    suspend fun getProfile(): Response<UserDto>

    @GET("profile/streak")
    suspend fun getStreak(): Response<StreakDto>

    @PUT("profile")
    suspend fun updateProfile(@Body profile: UpdateProfileRequest): Response<UserDto>

    @GET("profile/stats")
    suspend fun getStats(): Response<StatsDto>

    @GET("profile/badges")
    suspend fun getBadges(): Response<List<BadgeDto>>

    @GET("profile/weekly-challenge")
    suspend fun getWeeklyChallenge(): Response<WeeklyChallengeDto>

    @GET("tracking/mission/{missionId}")
    suspend fun getMissionWithClues(
        @Path("missionId") missionId: String
    ): Response<MissionWithCluesResponse>

    @POST("tracking/check-location")
    suspend fun checkLocation(
        @Body request: CheckLocationRequest
    ): Response<CheckLocationResponse>

    @POST("tracking/skip-clue")
    suspend fun skipClue(
        @Body request: SkipClueRequest
    ): Response<CheckLocationResponse>

    @POST("tracking/reset-progress")
    suspend fun resetMissionProgress(
        @Body request: ResetProgressRequest
    ): Response<Unit>

    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<Map<String, String>>

    @GET("suggestions")
    suspend fun getSuggestions(): Response<List<SuggestionDto>>

    @GET("suggestions/{id}")
    suspend fun getSuggestion(@Path("id") id: String): Response<SuggestionDto>

    @POST("suggestions")
    suspend fun createSuggestion(@Body request: CreateSuggestionRequest): Response<SuggestionDto>

    @PUT("suggestions/{id}")
    suspend fun updateSuggestion(
        @Path("id") id: String,
        @Body request: UpdateSuggestionRequest
    ): Response<SuggestionDto>

    @DELETE("suggestions/{id}")
    suspend fun deleteSuggestion(@Path("id") id: String): Response<Unit>
}

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class UploadResponse(
    val message: String,
    val imageUrl: String
)

data class GeneralResponse(
    val message: String
)