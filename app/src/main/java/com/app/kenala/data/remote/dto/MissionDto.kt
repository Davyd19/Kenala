package com.app.kenala.data.remote.dto

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