package com.app.kenala.data.local.dao

import androidx.room.*
import com.app.kenala.data.local.entities.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journals ORDER BY date DESC")
    fun getAllJournals(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journals WHERE id = :id")
    suspend fun getJournalById(id: String): JournalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournals(journals: List<JournalEntity>)

    @Update
    suspend fun updateJournal(journal: JournalEntity)

    @Delete
    suspend fun deleteJournal(journal: JournalEntity)

    @Query("DELETE FROM journals WHERE id = :id")
    suspend fun deleteJournalById(id: String)

    @Query("SELECT * FROM journals WHERE isSynced = 0")
    suspend fun getUnsyncedJournals(): List<JournalEntity>

    @Query("DELETE FROM journals")
    suspend fun deleteAllJournals()
}