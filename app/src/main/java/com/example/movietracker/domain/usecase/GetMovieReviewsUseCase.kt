package com.example.movietracker.domain.usecase

import com.example.movietracker.domain.model.Review
import com.example.movietracker.domain.repository.MovieRepository
import com.example.movietracker.util.Resource

class GetMovieReviewsUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(id: Int, mediaType: String): Resource<List<Review>> =
        repository.getMovieReviews(id, mediaType)
}
