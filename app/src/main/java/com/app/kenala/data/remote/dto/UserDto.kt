package com.app.kenala.data.remote.dto

data class UserDto(
    val id: String,
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

data class StatsDto(
    val level: Int,
    val total_missions: Int,
    val total_distance: Float,
    val current_streak: Int,
    val longest_streak: Int,
    val total_active_days: Int,
    val journal_count: Int,
    val category_breakdown: Map<String, Int>
)

data class BadgeDto(
    val id: String,
    val name: String,
    val description: String,
    val icon_name: String,
    val color: String,
    val is_unlocked: Boolean,
    val unlocked_at: String?
)