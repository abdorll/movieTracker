package com.example.movietracker.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val releaseDate: String,
    val runtime: Int?,       // minutes for movies, episode length for shows
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<Genre>,
    val tagline: String?,
    val cast: List<CastMember>,
    val mediaType: String,
    val trailerKey: String? = null
)

data class Genre(val id: Int, val name: String)
