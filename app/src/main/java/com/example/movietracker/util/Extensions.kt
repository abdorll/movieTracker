package com.example.movietracker.util

import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.model.MovieDetail

fun MovieDetail.toMovie() = Movie(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    mediaType = mediaType
)
