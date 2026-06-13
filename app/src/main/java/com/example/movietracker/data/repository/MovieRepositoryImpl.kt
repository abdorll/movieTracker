package com.example.movietracker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.movietracker.data.mapper.toMovieDetail
import com.example.movietracker.data.mapper.toReview
import com.example.movietracker.data.paging.MoviePagingSource
import com.example.movietracker.data.paging.SearchPagingSource
import com.example.movietracker.data.paging.ShowPagingSource
import com.example.movietracker.data.remote.api.TmdbApi
import com.example.movietracker.data.remote.dto.VideosResponseDto
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.model.MovieDetail
import com.example.movietracker.domain.model.Review
import com.example.movietracker.domain.repository.MovieRepository
import com.example.movietracker.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class MovieRepositoryImpl(
    private val api: TmdbApi
) : MovieRepository {

    override fun getTrendingMovies(): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false)
    ) {
        MoviePagingSource { page -> api.getTrendingMovies(page) }
    }.flow

    override fun getTrendingShows(): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false)
    ) {
        ShowPagingSource { page -> api.getTrendingShows(page) }
    }.flow

    override fun searchMulti(query: String): Flow<PagingData<Movie>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false)
    ) {
        SearchPagingSource { page -> api.searchMulti(query, page) }
    }.flow

    override suspend fun getMovieDetail(id: Int, mediaType: String): Resource<MovieDetail> =
        try {
            coroutineScope {
                val detailDeferred = async {
                    if (mediaType == "movie") api.getMovieDetail(id) else api.getShowDetail(id)
                }
                val creditsDeferred = async {
                    if (mediaType == "movie") api.getMovieCredits(id) else api.getShowCredits(id)
                }
                val videosDeferred = async {
                    try {
                        if (mediaType == "movie") api.getMovieVideos(id) else api.getShowVideos(id)
                    } catch (e: Exception) {
                        VideosResponseDto(emptyList())
                    }
                }
                Resource.Success(
                    detailDeferred.await().toMovieDetail(
                        cast = creditsDeferred.await().cast,
                        mediaType = mediaType,
                        trailerKey = videosDeferred.await().trailerKey()
                    )
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load details")
        }

    override suspend fun getMovieReviews(id: Int, mediaType: String): Resource<List<Review>> =
        try {
            val response = if (mediaType == "movie") api.getMovieReviews(id) else api.getShowReviews(id)
            Resource.Success(response.results.map { it.toReview() })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load reviews")
        }
}
