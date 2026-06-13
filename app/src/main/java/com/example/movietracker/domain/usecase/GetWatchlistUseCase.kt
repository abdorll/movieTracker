package com.example.movietracker.domain.usecase

import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

class GetWatchlistUseCase(
    private val repository: WatchlistRepository
) {
    operator fun invoke(): Flow<List<Movie>> = repository.getWatchlist()
}
