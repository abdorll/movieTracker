package com.example.movietracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity = a database table definition. Flutter equivalent: a Hive HiveObject or Isar schema.
// Room generates all the SQL CREATE TABLE statements from this annotation at compile time.
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val overview: String,
    val voteAverage: Double,
    val releaseDate: String,
    val mediaType: String,
    val addedAt: Long = System.currentTimeMillis()
)
