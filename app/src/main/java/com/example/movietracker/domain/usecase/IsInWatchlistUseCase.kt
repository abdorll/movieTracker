package com.example.movietracker.domain.usecase

import com.example.movietracker.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow

class IsInWatchlistUseCase(
    private val repository: WatchlistRepository
) {
    operator fun invoke(id: Int): Flow<Boolean> = repository.isInWatchlist(id)
}
