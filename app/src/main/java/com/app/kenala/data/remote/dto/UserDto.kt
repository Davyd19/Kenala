package com.app.kenala.data.remote.dto

import com.google.gson.annotations.SerializedName
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val bio: String?,
    val profile_image_url: String?,
    val total_missions: Int,
    val total_distance: Float,
    val current_streak: Int,
    val longest_streak: Int,
    val total_active_days: Int
)

data class StatsDto(
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

data class WeeklyChallengeDto(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val reward_points: Int,
    val expires_at: String
)

data class StreakDto(
    @SerializedName("current_streak") val currentStreak: Int,
    @SerializedName("longest_streak") val longestStreak: Int,
    @SerializedName("total_active_days") val totalActiveDays: Int,
    @SerializedName("last_active_date") val lastActiveDate: String?,
    @SerializedName("recent_activity") val recentActivity: Map<String, Boolean>
)