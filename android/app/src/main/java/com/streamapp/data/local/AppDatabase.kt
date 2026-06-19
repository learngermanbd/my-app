package com.streamapp.data.local

import androidx.room.*

@Database(
    entities = [CachedCategory::class, CachedChannel::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun streamDao(): StreamDao

    companion object {
        const val NAME = "streamapp_cache.db"
    }
}
