package com.example.movietracker.domain.usecase

import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.repository.WatchlistRepository

class AddToWatchlistUseCase(
    private val repository: WatchlistRepository
) {
    suspend operator fun invoke(movie: Movie) = repository.addToWatchlist(movie)
}
