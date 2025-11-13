package com.app.kenala.data.remote.dto

import com.google.gson.annotations.SerializedName

// Data yang diterima dari server
data class SuggestionDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("location_name") val locationName: String,
    val category: String,
    val description: String,
    val status: String, // "pending", "approved", "rejected"
    @SerializedName("created_at") val createdAt: String
)

// Data yang dikirim untuk membuat suggestion baru
data class CreateSuggestionRequest(
    @SerializedName("location_name") val locationName: String,
    val category: String,
    val description: String
)

// Data yang dikirim untuk update suggestion
data class UpdateSuggestionRequest(
    @SerializedName("location_name") val locationName: String,
    val category: String,
    val description: String
)