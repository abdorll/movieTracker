package com.example.movietracker.presentation.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietracker.domain.usecase.GetWatchlistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class WatchlistViewModel(
    getWatchlist: GetWatchlistUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(MediaFilter.ALL)

    val uiState = combine(getWatchlist(), _filter) { items, filter ->
        val filtered = when (filter) {
            MediaFilter.ALL -> items
            MediaFilter.MOVIES -> items.filter { it.mediaType == "movie" }
            MediaFilter.SHOWS -> items.filter { it.mediaType == "tv" }
        }
        WatchlistUiState(items = filtered, filter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WatchlistUiState()
    )

    fun setFilter(filter: MediaFilter) {
        _filter.value = filter
    }
}
