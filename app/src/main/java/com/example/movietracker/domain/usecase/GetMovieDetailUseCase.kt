package com.example.movietracker.domain.usecase

import com.example.movietracker.domain.model.MovieDetail
import com.example.movietracker.domain.repository.MovieRepository
import com.example.movietracker.util.Resource

class GetMovieDetailUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(id: Int, mediaType: String): Resource<MovieDetail> =
        repository.getMovieDetail(id, mediaType)
}
