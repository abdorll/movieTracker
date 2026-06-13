package com.example.movietracker.data.repository

import com.example.movietracker.data.local.dao.WatchlistDao
import com.example.movietracker.data.mapper.toMovie
import com.example.movietracker.data.mapper.toWatchlistEntity
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchlistRepositoryImpl(
    private val dao: WatchlistDao
) : WatchlistRepository {

    override fun getWatchlist(): Flow<List<Movie>> =
        dao.getAll().map { entities -> entities.map { it.toMovie() } }

    override suspend fun addToWatchlist(movie: Movie) =
        dao.insert(movie.toWatchlistEntity())

    override suspend fun removeFromWatchlist(id: Int) =
        dao.deleteById(id)

    override fun isInWatchlist(id: Int): Flow<Boolean> =
        dao.isInWatchlist(id)
}
