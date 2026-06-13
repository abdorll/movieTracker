package com.example.movietracker.presentation.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.movietracker.di.AppContainer
import com.example.movietracker.domain.model.Movie
import com.example.movietracker.presentation.components.ErrorView
import com.example.movietracker.presentation.components.LoadingIndicator
import com.example.movietracker.presentation.components.MovieCard
import com.example.movietracker.presentation.components.MovieTrackerLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onMovieClick: (Int, String) -> Unit,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.getTrendingMovies, AppContainer.getTrendingShows)
    }
) {
    val trendingMovies = viewModel.trendingMovies.collectAsLazyPagingItems()
    val trendingShows = viewModel.trendingShows.collectAsLazyPagingItems()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Movies", "TV Shows")
    val items: LazyPagingItems<Movie> = if (selectedTab == 0) trendingMovies else trendingShows

    val isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0

    Column(modifier = Modifier.fillMaxSize()) {
        // Header row: logo + theme toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MovieTrackerLogo()
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { items.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            MovieGrid(items = items, onMovieClick = onMovieClick)
        }
    }
}

@Composable
private fun MovieGrid(
    items: LazyPagingItems<Movie>,
    onMovieClick: (Int, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (items.loadState.refresh is LoadState.Loading && items.itemCount == 0) {
            item(span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
            return@LazyVerticalGrid
        }

        if (items.loadState.refresh is LoadState.Error && items.itemCount == 0) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorView(
                    message = "Failed to load. Check your connection.",
                    onRetry = { items.retry() }
                )
            }
            return@LazyVerticalGrid
        }

        items(count = items.itemCount) { index ->
            items[index]?.let { movie ->
                MovieCard(movie = movie, onClick = { onMovieClick(movie.id, movie.mediaType) })
            }
        }

        if (items.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) { LoadingIndicator() }
        }

        if (items.loadState.append is LoadState.Error) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ErrorView(
                    message = "Failed to load more.",
                    onRetry = { items.retry() }
                )
            }
        }
    }
}
