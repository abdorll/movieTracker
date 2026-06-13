package com.example.movietracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movietracker.data.local.dao.WatchlistDao
import com.example.movietracker.data.local.entity.WatchlistEntity

// @Database lists every table (entity) and the schema version.
// When you add a new table later, increment version and provide a Migration.
@Database(entities = [WatchlistEntity::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}
