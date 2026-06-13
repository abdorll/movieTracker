package com.example.movietracker.data.remote.dto

import com.google.gson.annotations.SerializedName

// TMDB wraps every paginated list in this envelope: { page, results, total_pages, total_results }
// The <T> generic means one class works for movies, shows, reviews, etc.
// Flutter equivalent: a generic ApiResponse<T> wrapper you'd write in Dart
data class PagedResponseDto<T>(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<T>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)
