package com.app.kenala.data.remote.dto

import com.google.gson.annotations.SerializedName

// File ini HANYA berisi Data (DTO) dan Response.
// Semua Request (CheckLocationRequest, dll) sudah dipindah ke RequestDtos.kt

// DTO Asli
data class MissionDto(
    val id: String,
    val name: String,
    val description: String?,
    val category: String,
    val location_name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val image_url: String?,
    val budget_category: String,
    val estimated_distance: Double?, // Ubah ke Double agar konsisten
    val difficulty_level: String,
    val points: Int,
    val is_active: Boolean
)

// --- DTO BARU UNTUK TRACKING MISI ---

data class MissionClueDto(
    val id: String,
    @SerializedName("clue_order") val clueOrder: Int,
    val name: String,
    val description: String,
    @SerializedName("hint_text") val hint: String?,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("radius_meters") val radius: Int,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("points_reward") val points: Int,
    @SerializedName("is_completed") val isCompleted: Boolean
)

data class MissionProgressDto(
    @SerializedName("completed_clues") val completedClues: Int,
    @SerializedName("total_clues") val totalClues: Int,
    @SerializedName("is_mission_completed") val isMissionCompleted: Boolean
)

// DTO untuk Response GET /api/tracking/mission/:id
data class MissionWithCluesResponse(
    val mission: MissionDto,
    val clues: List<MissionClueDto>,
    val progress: MissionProgressDto
)

// DTO untuk Response POST /api/tracking/check-location
data class CheckLocationResponse(
    val status: String, // "tracking", "clue_reached", "all_clues_completed"
    @SerializedName("clue_reached") val clueReached: Boolean,
    @SerializedName("current_clue") val currentClue: CurrentClueDto?,
    val distance: DistanceDto,
    val progress: ClueProgressDto,
    val destination: DestinationDto?
)

data class CurrentClueDto(
    val id: String,
    val order: Int,
    val name: String,
    val description: String,
    val hint: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val latitude: String,
    val longitude: String,
    val radius: Int,
    val points: Int
)

data class DistanceDto(
    val meters: Double,
    val formatted: String,
    val message: String
)

data class ClueProgressDto(
    val completed: Int,
    val total: Int,
    @SerializedName("next_clue_number") val nextClueNumber: Int?
)

data class DestinationDto(
    val name: String,
    val latitude: String,
    val longitude: String,
    val distance: Double,
    @SerializedName("formatted_distance") val formattedDistance: String,
    val message: String,
    @SerializedName("is_arrived") val isArrived: Boolean
)