package com.app.kenala.data.remote.dto

import com.google.gson.annotations.SerializedName

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
    val estimated_distance: Float?,
    val difficulty_level: String,
    val points: Int,
    val is_active: Boolean
)

// --- DTO BARU UNTUK TRACKING MISI ---

// DTO untuk satu Clue
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
    @SerializedName("is_completed") val isCompleted: Boolean // Ini dari controller, bukan model
)

// DTO untuk Progress Misi (Bagian dari response GetMissionWithClues)
data class MissionProgressDto(
    @SerializedName("completed_clues") val completedClues: Int,
    @SerializedName("total_clues") val totalClues: Int,
    @SerializedName("is_mission_completed") val isMissionCompleted: Boolean
)

// DTO untuk Response GET /api/tracking/mission/:id
data class MissionWithCluesResponse(
    val mission: MissionDto,
    val clues: List<MissionClueDto>, // Backend mengirim clues di luar mission
    val progress: MissionProgressDto
)

// DTO untuk Request POST /api/tracking/check-location
data class CheckLocationRequest(
    @SerializedName("mission_id") val missionId: String,
    val latitude: Double,
    val longitude: Double
)

// DTO untuk Response POST /api/tracking/check-location
data class CheckLocationResponse(
    val status: String, // "tracking", "clue_reached", "all_clues_completed"
    @SerializedName("clue_reached") val clueReached: Boolean,
    @SerializedName("current_clue") val currentClue: CurrentClueDto?,
    val distance: DistanceDto,
    val progress: ClueProgressDto,
    val destination: DestinationDto? // Hanya ada jika status 'all_clues_completed'
)

data class CurrentClueDto(
    val id: String,
    val order: Int,
    val name: String,
    val description: String,
    val hint: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val latitude: String, // API mengirim ini sebagai String
    val longitude: String, // API mengirim ini sebagai String
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
    val latitude: String, // API mengirim ini sebagai String
    val longitude: String, // API mengirim ini sebagai String
    val distance: Double,
    @SerializedName("formatted_distance") val formattedDistance: String,
    val message: String,
    @SerializedName("is_arrived") val isArrived: Boolean
)