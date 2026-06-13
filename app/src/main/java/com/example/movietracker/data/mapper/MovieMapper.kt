package com.example.movietracker.data.mapper

import com.example.movietracker.data.remote.dto.CastDto
import com.example.movietracker.data.remote.dto.MovieDetailDto
import com.example.movietracker.data.remote.dto.MovieDto
import com.example.movietracker.data.remote.dto.MultiSearchResultDto
import com.example.movietracker.data.remote.dto.ReviewDto
import com.example.movietracker.data.remote.dto.ShowDto
import com.example.movietracker.domain.model.CastMember
import com.example.movietracker.domain.model.Genre
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.model.MovieDetail
import com.example.movietracker.domain.model.Review

// Mappers: the translation layer between raw API data and clean domain models.
// Flutter equivalent: the fromJson/toJson factories on your model classes,
// except here we keep them OUT of the model to preserve Clean Architecture separation.

fun MovieDto.toMovie() = Movie(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate.orEmpty(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    mediaType = "movie"
)

fun ShowDto.toMovie() = Movie(
    id = id,
    title = name,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = firstAirDate.orEmpty(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    mediaType = "tv"
)

// Returns null for "person" results — callers use mapNotNull to drop them
fun MultiSearchResultDto.toMovie(): Movie? {
    if (mediaType !in listOf("movie", "tv")) return null
    return Movie(
        id = id,
        title = title ?: name ?: return null,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview.orEmpty(),
        releaseDate = releaseDate ?: firstAirDate ?: "",
        voteAverage = voteAverage ?: 0.0,
        voteCount = voteCount ?: 0,
        mediaType = mediaType
    )
}

fun MovieDetailDto.toMovieDetail(cast: List<CastDto>, mediaType: String, trailerKey: String? = null) = MovieDetail(
    id = id,
    title = title ?: name.orEmpty(),
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate ?: firstAirDate.orEmpty(),
    runtime = runtime ?: episodeRunTime?.firstOrNull(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.map { Genre(it.id, it.name) },
    tagline = tagline,
    cast = cast.map { it.toCastMember() },
    mediaType = mediaType,
    trailerKey = trailerKey
)

fun CastDto.toCastMember() = CastMember(
    id = id,
    name = name,
    character = character,
    profilePath = profilePath
)

fun ReviewDto.toReview() = Review(
    id = id,
    author = author,
    content = content,
    createdAt = createdAt,
    rating = authorDetails?.rating
)
