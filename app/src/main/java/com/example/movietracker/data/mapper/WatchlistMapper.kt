package com.example.movietracker.data.mapper

import com.example.movietracker.data.local.entity.WatchlistEntity
import com.example.movietracker.domain.model.Movie

fun WatchlistEntity.toMovie() = Movie(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = null,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = 0,
    mediaType = mediaType
)

fun Movie.toWatchlistEntity() = WatchlistEntity(
    id = id,
    title = title,
    posterPath = posterPath,
    overview = overview,
    voteAverage = voteAverage,
    releaseDate = releaseDate,
    mediaType = mediaType
)

