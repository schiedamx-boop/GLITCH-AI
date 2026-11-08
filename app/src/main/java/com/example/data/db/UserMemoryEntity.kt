package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val category: String = "GENERAL", // e.g., "IDENTITY", "PREFERENCE", "FACT", "MOOD"
    val timestamp: Long = System.currentTimeMillis()
)
