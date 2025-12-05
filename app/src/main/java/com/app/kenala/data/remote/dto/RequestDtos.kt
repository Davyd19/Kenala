package com.app.kenala.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * File ini berisi semua data class untuk Request Body ke API
 * agar tidak terjadi duplikasi atau mismatch package.
 */

// --- Auth Requests ---
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UpdateProfileRequest(
    val name: String?,
    val phone: String?,
    val bio: String?,
    val profile_image_url: String?
)

// --- Mission Requests ---
data class CheckLocationRequest(
    @SerializedName("mission_id") val missionId: String,
    val latitude: Double,
    val longitude: Double
)

data class SkipClueRequest(
    @SerializedName("mission_id") val missionId: String,
    @SerializedName("clue_id") val clueId: String
)

data class ResetProgressRequest(
    @SerializedName("mission_id") val missionId: String
)

data class CompleteMissionRequest(
    @SerializedName("mission_id") val missionId: String,
    @SerializedName("real_distance_meters") val realDistanceMeters: Double? = 0.0
)

// --- Journal Requests ---
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

// --- Suggestion Requests ---
data class CreateSuggestionRequest(
    @SerializedName("location_name") val locationName: String,
    val address: String?,
    val category: String,
    val description: String
)

data class UpdateSuggestionRequest(
    @SerializedName("location_name") val locationName: String,
    val address: String?,
    val category: String,
    val description: String
)