package com.example.movietracker.presentation.screens.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movietracker.di.AppContainer
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.movietracker.presentation.components.ErrorView
import com.example.movietracker.presentation.components.LoadingIndicator
import com.example.movietracker.presentation.components.MovieCard
import com.example.movietracker.presentation.components.MovieTrackerLogo

@Composable
fun SearchScreen(
    onMovieClick: (Int, String) -> Unit,
    viewModel: SearchViewModel = viewModel {
        SearchViewModel(AppContainer.searchMovies)
    }
) {
    val query by viewModel.query.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        MovieTrackerLogo(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Pill-shaped search bar
        TextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp)),
            placeholder = { Text("Search movies & TV shows...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        )

        when {
            query.isBlank() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            "Discover movies and TV shows",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    if (searchResults.loadState.refresh is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
                        return@LazyVerticalGrid
                    }

                    if (searchResults.loadState.refresh is LoadState.Error) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorView("Search failed. Try again.") { searchResults.retry() }
                        }
                        return@LazyVerticalGrid
                    }

                    if (searchResults.itemCount == 0 &&
                        searchResults.loadState.refresh is LoadState.NotLoading
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No results for \"$query\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        return@LazyVerticalGrid
                    }

                    items(count = searchResults.itemCount) { index ->
                        searchResults[index]?.let { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id, movie.mediaType) }
                            )
                        }
                    }

                    if (searchResults.loadState.append is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
                    }
                }
            }
        }
    }
}
