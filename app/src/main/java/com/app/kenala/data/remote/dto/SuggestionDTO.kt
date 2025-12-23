package com.app.kenala.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SuggestionDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("location_name") val locationName: String,
    val address: String?,
    val category: String,
    val description: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String
)