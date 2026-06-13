package com.example.movietracker.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.usecase.GetTrendingMoviesUseCase
import com.example.movietracker.domain.usecase.GetTrendingShowsUseCase
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    getTrendingMovies: GetTrendingMoviesUseCase,
    getTrendingShows: GetTrendingShowsUseCase
) : ViewModel() {

    val trendingMovies: Flow<PagingData<Movie>> = getTrendingMovies()
        .cachedIn(viewModelScope)

    val trendingShows: Flow<PagingData<Movie>> = getTrendingShows()
        .cachedIn(viewModelScope)
}
