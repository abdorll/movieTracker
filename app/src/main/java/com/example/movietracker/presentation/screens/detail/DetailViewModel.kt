package com.example.movietracker.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietracker.domain.usecase.AddToWatchlistUseCase
import com.example.movietracker.domain.usecase.GetMovieDetailUseCase
import com.example.movietracker.domain.usecase.GetMovieReviewsUseCase
import com.example.movietracker.domain.usecase.IsInWatchlistUseCase
import com.example.movietracker.domain.usecase.RemoveFromWatchlistUseCase
import com.example.movietracker.util.Resource
import com.example.movietracker.util.toMovie
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getMovieDetail: GetMovieDetailUseCase,
    private val getMovieReviews: GetMovieReviewsUseCase,
    private val addToWatchlist: AddToWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val isInWatchlist: IsInWatchlistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var watchlistJob: Job? = null

    fun loadDetail(id: Int, mediaType: String) {
        watchlistJob?.cancel()

        // If we already have data this is a pull-to-refresh: keep content visible, show the
        // pull indicator instead of replacing the whole screen with a spinner.
        val isRefresh = _uiState.value.movieDetail != null
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            when (val result = getMovieDetail(id, mediaType)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, movieDetail = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                is Resource.Loading -> {}
            }

            when (val result = getMovieReviews(id, mediaType)) {
                is Resource.Success -> _uiState.update { it.copy(reviews = result.data) }
                else -> {}
            }
        }

        watchlistJob = viewModelScope.launch {
            isInWatchlist(id).collect { inWatchlist ->
                _uiState.update { it.copy(isInWatchlist = inWatchlist) }
            }
        }
    }

    fun toggleWatchlist() {
        val detail = _uiState.value.movieDetail ?: return
        viewModelScope.launch {
            if (_uiState.value.isInWatchlist) {
                removeFromWatchlist(detail.id)
            } else {
                addToWatchlist(detail.toMovie())
            }
        }
    }
}
