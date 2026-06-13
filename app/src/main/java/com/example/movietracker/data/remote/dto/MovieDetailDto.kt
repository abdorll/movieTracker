package com.example.movietracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MovieDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,         // movies
    @SerializedName("name") val name: String?,           // tv shows
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("overview") val overview: String,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int,
    @SerializedName("genres") val genres: List<GenreDto>,
    @SerializedName("tagline") val tagline: String?
)

data class GenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class CreditsDto(
    @SerializedName("cast") val cast: List<CastDto>
)

data class VideoDto(
    @SerializedName("key") val key: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String,
    @SerializedName("official") val official: Boolean = false
)

data class VideosResponseDto(
    @SerializedName("results") val results: List<VideoDto>
) {
    // Prefer official YouTube trailers; fall back to any YouTube video
    fun trailerKey(): String? =
        results.firstOrNull { it.site == "YouTube" && it.type == "Trailer" && it.official }?.key
            ?: results.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
            ?: results.firstOrNull { it.site == "YouTube" }?.key
}

data class CastDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?
)
