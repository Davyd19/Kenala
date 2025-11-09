package com.app.kenala.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val bio: String?,
    val profile_image_url: String?,
    val level: Int,
    val total_missions: Int,
    val total_distance: Float,
    val current_streak: Int,
    val longest_streak: Int,
    val total_active_days: Int
)