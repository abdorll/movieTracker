package com.example.movietracker.presentation.screens.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movietracker.di.AppContainer
import com.example.movietracker.domain.model.CastMember
import com.example.movietracker.domain.model.MovieDetail
import com.example.movietracker.domain.model.Review
import com.example.movietracker.presentation.components.ErrorView
import com.example.movietracker.presentation.components.LoadingIndicator
import com.example.movietracker.presentation.components.RatingBar
import com.example.movietracker.presentation.components.YoutubePlayer
import com.example.movietracker.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movieId: Int,
    mediaType: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel {
        DetailViewModel(
            AppContainer.getMovieDetail,
            AppContainer.getMovieReviews,
            AppContainer.addToWatchlist,
            AppContainer.removeFromWatchlist,
            AppContainer.isInWatchlist
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.loadDetail(movieId, mediaType)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                ErrorView(
                    message = uiState.error!!,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.movieDetail != null -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.loadDetail(movieId, mediaType) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    DetailContent(
                        detail = uiState.movieDetail!!,
                        reviews = uiState.reviews
                    )
                }
                FloatingActionButton(
                    onClick = viewModel::toggleWatchlist,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isInWatchlist) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = if (uiState.isInWatchlist) "Remove from watchlist"
                        else "Add to watchlist"
                    )
                }
            }
        }

        // Always white + dark circle so it's readable over any backdrop in any theme
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun DetailContent(detail: MovieDetail, reviews: List<Review>) {
    val context = LocalContext.current

    // showTrailer: true while the in-app player is visible
    // trailerBlocked: true if the player returned error 150/152 (embedding disabled by uploader).
    //   In that case the play button opens YouTube directly instead of trying the player again.
    var showTrailer   by remember { mutableStateOf(false) }
    var trailerBlocked by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val trailerKey = detail.trailerKey

            if (showTrailer && trailerKey != null) {
                // The player is a direct LazyColumn child so it receives its OWN size
                // constraints (fillMaxWidth + aspectRatio). Nesting it inside another Box
                // with duplicate aspectRatio causes competing constraints that collapse
                // the View to zero height — the root cause of the blank-screen bug.
                YoutubePlayer(
                    videoId = trailerKey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    onError = {
                        // The video owner has disabled embedding. Reset to backdrop and
                        // let the next tap go straight to the YouTube app.
                        showTrailer    = false
                        trailerBlocked = true
                    }
                )
            } else {
                // Backdrop image + overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(detail.backdropPath?.let { "${Constants.BACKDROP_BASE_URL}$it" })
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (trailerKey != null) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.30f))
                                .clickable {
                                    if (trailerBlocked) {
                                        // Embedding disabled — open in YouTube app / browser
                                        val appIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("vnd.youtube:$trailerKey")
                                        )
                                        val webIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://www.youtube.com/watch?v=$trailerKey")
                                        )
                                        try { context.startActivity(appIntent) }
                                        catch (e: ActivityNotFoundException) { context.startActivity(webIntent) }
                                    } else {
                                        showTrailer = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = if (trailerBlocked) Icons.AutoMirrored.Filled.OpenInNew
                                                  else Icons.Default.PlayArrow,
                                    contentDescription = if (trailerBlocked) "Watch on YouTube"
                                                         else "Watch trailer",
                                    tint = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(detail.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (!detail.tagline.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        detail.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = detail.voteAverage, starSize = 16.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${detail.voteCount} votes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (detail.runtime != null) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${detail.runtime} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    detail.genres.take(4).forEach { genre ->
                        FilterChip(selected = false, onClick = {}, label = { Text(genre.name) })
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(detail.overview, style = MaterialTheme.typography.bodyMedium)

                if (detail.releaseDate.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Release: ${detail.releaseDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (detail.cast.isNotEmpty()) {
            item {
                Text(
                    "Cast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    detail.cast.take(10).forEach { cast -> CastCard(cast) }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        if (reviews.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            items(reviews) { review -> ReviewItem(review = review) }
        }

        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun CastCard(cast: CastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(cast.profilePath?.let { "${Constants.PROFILE_BASE_URL}$it" })
                .crossfade(true)
                .build(),
            contentDescription = cast.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(4.dp))
        Text(cast.name, style = MaterialTheme.typography.labelSmall, maxLines = 2)
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.author,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (review.rating != null) RatingBar(rating = review.rating)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = review.content.take(200) + if (review.content.length > 200) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
