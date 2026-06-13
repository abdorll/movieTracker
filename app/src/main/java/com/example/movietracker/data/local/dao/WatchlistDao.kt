package com.example.movietracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movietracker.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

// @Dao = Data Access Object. All your database queries live here.
// Flutter equivalent: a Hive Box or Isar collection's query methods.
// Room generates the SQLite implementation of every method here at compile time.
@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    // OnConflictStrategy.REPLACE: if item already exists, update it instead of throwing an error
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Returns a Flow<Boolean> so the UI reactively updates the bookmark icon in real-time
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id)")
    fun isInWatchlist(id: Int): Flow<Boolean>
}
