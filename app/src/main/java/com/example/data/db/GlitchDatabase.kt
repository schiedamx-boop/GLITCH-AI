package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        UserMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GlitchDatabase : RoomDatabase() {
    abstract fun glitchDao(): GlitchDao

    companion object {
        @Volatile
        private var INSTANCE: GlitchDatabase? = null

        fun getInstance(context: Context): GlitchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GlitchDatabase::class.java,
                    "glitch_ai_memory.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
