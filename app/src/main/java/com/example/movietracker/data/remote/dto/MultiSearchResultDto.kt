package com.example.movietracker.data.remote.dto

import com.google.gson.annotations.SerializedName

// /search/multi returns movies, TV shows, AND people in one list.
// media_type tells us which shape each result is. We filter out "person".
data class MultiSearchResultDto(
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val mediaType: String,
    // movie fields
    @SerializedName("title") val title: String?,
    @SerializedName("release_date") val releaseDate: String?,
    // tv fields
    @SerializedName("name") val name: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    // shared
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?
)
