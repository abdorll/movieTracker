package com.example.movietracker.presentation.screens.detail

import com.example.movietracker.domain.model.MovieDetail
import com.example.movietracker.domain.model.Review

data class DetailUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val reviews: List<Review> = emptyList(),
    val isInWatchlist: Boolean = false,
    val error: String? = null
)
