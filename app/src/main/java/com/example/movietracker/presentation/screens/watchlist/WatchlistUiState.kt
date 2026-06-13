package com.example.movietracker.presentation.screens.watchlist

import com.example.movietracker.domain.model.Movie

enum class MediaFilter(val label: String) {
    ALL("All"),
    MOVIES("Movies"),
    SHOWS("TV Shows")
}

data class WatchlistUiState(
    val items: List<Movie> = emptyList(),
    val filter: MediaFilter = MediaFilter.ALL
)
