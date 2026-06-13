package com.example.movietracker.di

import android.content.Context
import androidx.room.Room
import com.example.movietracker.BuildConfig
import com.example.movietracker.data.local.MovieDatabase
import com.example.movietracker.data.remote.api.TmdbApi
import com.example.movietracker.data.remote.interceptor.AuthInterceptor
import com.example.movietracker.data.repository.MovieRepositoryImpl
import com.example.movietracker.data.repository.WatchlistRepositoryImpl
import com.example.movietracker.domain.repository.MovieRepository
import com.example.movietracker.domain.repository.WatchlistRepository
import com.example.movietracker.domain.usecase.AddToWatchlistUseCase
import com.example.movietracker.domain.usecase.GetMovieDetailUseCase
import com.example.movietracker.domain.usecase.GetMovieReviewsUseCase
import com.example.movietracker.domain.usecase.GetTrendingMoviesUseCase
import com.example.movietracker.domain.usecase.GetTrendingShowsUseCase
import com.example.movietracker.domain.usecase.GetWatchlistUseCase
import com.example.movietracker.domain.usecase.IsInWatchlistUseCase
import com.example.movietracker.domain.usecase.RemoveFromWatchlistUseCase
import com.example.movietracker.domain.usecase.SearchMoviesUseCase
import com.example.movietracker.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Manual dependency injection — replaces Hilt for now.
// Flutter equivalent: a top-level GetIt container or Riverpod's ProviderContainer.
//
// `by lazy` = the object is created only on first access, then cached forever.
// This gives us the same app-scoped singleton behaviour as @Singleton in Hilt.
object AppContainer {

    private lateinit var appContext: Context

    // Called once from MovieTrackerApp.onCreate()
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // ── Network ──────────────────────────────────────────────────────────────

    private val authInterceptor by lazy {
        AuthInterceptor(BuildConfig.TMDB_API_KEY)
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tmdbApi: TmdbApi by lazy { retrofit.create(TmdbApi::class.java) }

    // ── Database ─────────────────────────────────────────────────────────────

    val database: MovieDatabase by lazy {
        Room.databaseBuilder(appContext, MovieDatabase::class.java, "movie_tracker.db").build()
    }

    private val watchlistDao by lazy { database.watchlistDao() }

    // ── Repositories ─────────────────────────────────────────────────────────

    val movieRepository: MovieRepository by lazy { MovieRepositoryImpl(tmdbApi) }
    val watchlistRepository: WatchlistRepository by lazy { WatchlistRepositoryImpl(watchlistDao) }

    // ── Use Cases ─────────────────────────────────────────────────────────────

    val getTrendingMovies by lazy { GetTrendingMoviesUseCase(movieRepository) }
    val getTrendingShows by lazy { GetTrendingShowsUseCase(movieRepository) }
    val searchMovies by lazy { SearchMoviesUseCase(movieRepository) }
    val getMovieDetail by lazy { GetMovieDetailUseCase(movieRepository) }
    val getMovieReviews by lazy { GetMovieReviewsUseCase(movieRepository) }
    val getWatchlist by lazy { GetWatchlistUseCase(watchlistRepository) }
    val addToWatchlist by lazy { AddToWatchlistUseCase(watchlistRepository) }
    val removeFromWatchlist by lazy { RemoveFromWatchlistUseCase(watchlistRepository) }
    val isInWatchlist by lazy { IsInWatchlistUseCase(watchlistRepository) }
}
