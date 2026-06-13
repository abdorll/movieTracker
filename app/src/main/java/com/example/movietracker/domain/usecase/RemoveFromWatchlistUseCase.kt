package com.example.movietracker.domain.usecase

import com.example.movietracker.domain.repository.WatchlistRepository

class RemoveFromWatchlistUseCase(
    private val repository: WatchlistRepository
) {
    suspend operator fun invoke(id: Int) = repository.removeFromWatchlist(id)
}
