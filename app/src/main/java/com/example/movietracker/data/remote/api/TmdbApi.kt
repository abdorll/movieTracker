package com.example.movietracker.data.remote.api

import com.example.movietracker.data.remote.dto.CreditsDto
import com.example.movietracker.data.remote.dto.MovieDetailDto
import com.example.movietracker.data.remote.dto.MovieDto
import com.example.movietracker.data.remote.dto.MultiSearchResultDto
import com.example.movietracker.data.remote.dto.PagedResponseDto
import com.example.movietracker.data.remote.dto.ReviewDto
import com.example.movietracker.data.remote.dto.ShowDto
import com.example.movietracker.data.remote.dto.VideosResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Flutter equivalent: an abstract class with methods annotated for dio/http
// Retrofit reads these annotations at compile time and generates the actual HTTP client for you
interface TmdbApi {

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(@Query("page") page: Int = 1): PagedResponseDto<MovieDto>

    @GET("trending/tv/day")
    suspend fun getTrendingShows(@Query("page") page: Int = 1): PagedResponseDto<ShowDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): PagedResponseDto<MultiSearchResultDto>

    @GET("movie/{id}")
    suspend fun getMovieDetail(@Path("id") id: Int): MovieDetailDto

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(@Path("id") id: Int): CreditsDto

    @GET("movie/{id}/reviews")
    suspend fun getMovieReviews(
        @Path("id") id: Int,
        @Query("page") page: Int = 1
    ): PagedResponseDto<ReviewDto>

    @GET("tv/{id}")
    suspend fun getShowDetail(@Path("id") id: Int): MovieDetailDto

    @GET("tv/{id}/credits")
    suspend fun getShowCredits(@Path("id") id: Int): CreditsDto

    @GET("tv/{id}/reviews")
    suspend fun getShowReviews(
        @Path("id") id: Int,
        @Query("page") page: Int = 1
    ): PagedResponseDto<ReviewDto>

    @GET("movie/{id}/videos")
    suspend fun getMovieVideos(@Path("id") id: Int): VideosResponseDto

    @GET("tv/{id}/videos")
    suspend fun getShowVideos(@Path("id") id: Int): VideosResponseDto
}
