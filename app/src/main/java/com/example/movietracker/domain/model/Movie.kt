package com.example.movietracker.domain.model

// Flutter equivalent: a plain Dart model class (no fromJson here — that lives in the data layer)
data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val mediaType: String  // "movie" or "tv"
)
