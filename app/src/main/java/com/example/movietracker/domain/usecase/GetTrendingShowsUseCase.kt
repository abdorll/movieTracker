package com.example.movietracker.domain.usecase

import androidx.paging.PagingData
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetTrendingShowsUseCase(
    private val repository: MovieRepository
) {
    operator fun invoke(): Flow<PagingData<Movie>> = repository.getTrendingShows()
}
