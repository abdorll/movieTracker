package com.example.movietracker.domain.repository

import androidx.paging.PagingData
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.model.MovieDetail
import com.example.movietracker.domain.model.Review
import com.example.movietracker.util.Resource
import kotlinx.coroutines.flow.Flow

// Flutter equivalent: an abstract class (service/repository interface) in your domain layer
// The data layer implements this; the domain layer only knows this interface — never the impl
interface MovieRepository {
    fun getTrendingMovies(): Flow<PagingData<Movie>>
    fun getTrendingShows(): Flow<PagingData<Movie>>
    fun searchMulti(query: String): Flow<PagingData<Movie>>
    suspend fun getMovieDetail(id: Int, mediaType: String): Resource<MovieDetail>
    suspend fun getMovieReviews(id: Int, mediaType: String): Resource<List<Review>>
}
