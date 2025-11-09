package com.app.kenala.data.remote.dto

data class JournalDto(
    val id: String,
    val user_id: String,
    val mission_id: String?,
    val title: String,
    val story: String,
    val image_url: String?,
    val location_name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val date: String,
    val created_at: String,
    val updated_at: String,
    val location: LocationDto? = null
)

data class LocationDto(
    val name: String?,
    val latitude: Double?,
    val longitude: Double?
)