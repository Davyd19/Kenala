package com.app.kenala.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.kenala.data.local.dao.JournalDao
import com.app.kenala.data.local.dao.NotificationDao
import com.app.kenala.data.local.dao.UserDao
import com.app.kenala.data.local.entities.JournalEntity
import com.app.kenala.data.local.entities.NotificationEntity
import com.app.kenala.data.local.entities.UserEntity

@Database(
    entities = [JournalEntity::class, UserEntity::class, NotificationEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kenala_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}