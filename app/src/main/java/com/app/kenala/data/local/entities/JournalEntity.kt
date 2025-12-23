package com.app.kenala.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val story: String,
    val date: String,
    val imageUrl: String?,
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isSynced: Boolean = false
)