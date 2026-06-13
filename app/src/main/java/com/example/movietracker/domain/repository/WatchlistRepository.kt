package com.example.movietracker.domain.repository

import com.example.movietracker.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlist(): Flow<List<Movie>>
    suspend fun addToWatchlist(movie: Movie)
    suspend fun removeFromWatchlist(id: Int)
    fun isInWatchlist(id: Int): Flow<Boolean>
}
