package com.example.movietracker.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.domain.usecase.SearchMoviesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchMovies: SearchMoviesUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // debounce(500ms) → skip blanks → flatMapLatest cancels in-flight call when query changes
    val searchResults: Flow<PagingData<Movie>> = _query
        .debounce(500L)
        .filter { it.isNotBlank() }
        .flatMapLatest { query -> searchMovies(query) }
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
