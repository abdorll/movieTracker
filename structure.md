# MovieTracker — Project Structure & Plan

## What You Need to Provide
1. **TMDB API Key** — free at https://www.themoviedb.org/settings/api (takes 2 min, use "Developer" option)
   - You'll paste it into `local.properties` as: `TMDB_API_KEY=your_key_here`
2. **Android Studio** with an emulator or physical device running API 24+
3. Nothing else — no auth, no backend, no paid services.

---

## Architecture: Clean Architecture + MVVM

Three layers, strict one-way dependency rule (Presentation → Domain ← Data):

```
┌─────────────────────────────┐
│     Presentation Layer      │  Composables + ViewModels + UiState
│  (screens, components, nav) │  Knows: Domain models only
├─────────────────────────────┤
│       Domain Layer          │  UseCases + Repository interfaces + Domain models
│  (pure Kotlin, no Android)  │  Knows: nothing outside itself
├─────────────────────────────┤
│        Data Layer           │  Retrofit (remote) + Room (local) + Repos
│  (API, DB, repo impls)      │  Knows: Domain interfaces it implements
└─────────────────────────────┘
```

**Flutter analogy:** Domain = your plain Dart models/services. Data = your http/Hive layer. Presentation = your widgets + providers.

---

## Full Package Structure

```
app/src/main/java/com/example/movietracker/
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── WatchlistDao.kt          # Room queries (like Hive box methods)
│   │   ├── entity/
│   │   │   └── WatchlistEntity.kt       # Room table definition
│   │   └── MovieDatabase.kt             # Room database singleton
│   │
│   ├── remote/
│   │   ├── api/
│   │   │   └── TmdbApi.kt               # Retrofit interface (all HTTP endpoints)
│   │   ├── dto/
│   │   │   ├── MovieDto.kt              # Raw API response shapes
│   │   │   ├── ShowDto.kt
│   │   │   ├── MovieDetailDto.kt
│   │   │   ├── CastDto.kt
│   │   │   ├── ReviewDto.kt
│   │   │   └── PagedResponseDto.kt      # Wrapper: { results, page, total_pages }
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt       # Injects API key into every request
│   │
│   ├── paging/
│   │   ├── MoviePagingSource.kt         # Trending/Popular feed pagination
│   │   └── SearchPagingSource.kt        # Search results pagination
│   │
│   ├── mapper/
│   │   ├── MovieMapper.kt               # DTO → Domain model conversions
│   │   └── WatchlistMapper.kt           # Entity ↔ Domain model conversions
│   │
│   └── repository/
│       ├── MovieRepositoryImpl.kt
│       └── WatchlistRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Movie.kt                     # Core domain model (no API/DB fields)
│   │   ├── MovieDetail.kt
│   │   ├── CastMember.kt
│   │   └── Review.kt
│   │
│   ├── repository/
│   │   ├── MovieRepository.kt           # Interface (contract)
│   │   └── WatchlistRepository.kt       # Interface (contract)
│   │
│   └── usecase/
│       ├── GetTrendingMoviesUseCase.kt
│       ├── GetTrendingShowsUseCase.kt
│       ├── SearchMoviesUseCase.kt
│       ├── GetMovieDetailUseCase.kt
│       ├── GetMovieReviewsUseCase.kt
│       ├── GetWatchlistUseCase.kt
│       ├── AddToWatchlistUseCase.kt
│       ├── RemoveFromWatchlistUseCase.kt
│       └── IsInWatchlistUseCase.kt
│
├── presentation/
│   ├── navigation/
│   │   ├── NavGraph.kt                  # All routes wired together
│   │   └── Screen.kt                    # Sealed class of route strings
│   │
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt            # Trending movies + shows tabs
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeUiState.kt
│   │   │
│   │   ├── search/
│   │   │   ├── SearchScreen.kt          # Search bar + results grid
│   │   │   ├── SearchViewModel.kt
│   │   │   └── SearchUiState.kt
│   │   │
│   │   ├── detail/
│   │   │   ├── DetailScreen.kt          # Poster, info, cast, reviews
│   │   │   ├── DetailViewModel.kt
│   │   │   └── DetailUiState.kt
│   │   │
│   │   ├── watchlist/
│   │   │   ├── WatchlistScreen.kt       # Saved movies/shows
│   │   │   ├── WatchlistViewModel.kt
│   │   │   └── WatchlistUiState.kt
│   │   │
│   │   └── ratings/
│   │       ├── RatingsScreen.kt         # TMDB reviews for a movie
│   │       └── RatingsViewModel.kt
│   │
│   └── components/                      # Reusable composables (= Flutter widgets)
│       ├── MovieCard.kt                 # Poster + title card
│       ├── RatingBar.kt
│       ├── LoadingIndicator.kt
│       ├── ErrorView.kt
│       └── PosterImage.kt               # Coil image with shimmer placeholder
│
├── di/                                  # Hilt modules (= get_it/Riverpod setup)
│   ├── NetworkModule.kt                 # Provides Retrofit, OkHttp, TmdbApi
│   ├── DatabaseModule.kt                # Provides Room DB, DAOs
│   └── RepositoryModule.kt              # Binds interfaces to implementations
│
├── util/
│   ├── Constants.kt                     # BASE_URL, image base URLs, etc.
│   ├── Resource.kt                      # Sealed class: Loading | Success | Error
│   └── Extensions.kt                    # Kotlin extension functions
│
└── MovieTrackerApp.kt                   # @HiltAndroidApp Application class
```

---

## Features Breakdown

### 1. Home Feed (Trending + Popular)
- Two tabs: Movies | TV Shows
- Paginated lazy grid (Paging 3 — loads next page as you scroll)
- Each card: poster image, title, star rating
- **Flutter analogy:** Like `ListView.builder` + `ScrollController` but automatic

### 2. Search
- Search bar with 500ms debounce (no API call on every keystroke)
- Paginated results grid
- Empty state + loading shimmer
- Searches both movies and TV shows

### 3. Detail Screen
- Hero poster image
- Title, tagline, release date, runtime, genres
- Overview text (expandable)
- Cast horizontal scroll row
- TMDB user reviews section
- "Add to Watchlist" FAB button (toggles saved state)

### 4. Watchlist (Room DB — local, no auth needed)
- Persisted locally with Room (SQLite under the hood)
- Filter by: All | Movies | TV Shows
- Swipe-to-remove
- Empty state illustration

### 5. Ratings & Reviews
- Fetched from TMDB's `/movie/{id}/reviews` endpoint
- Reviewer name, avatar, rating, content, date
- Shown as a section inside Detail screen + dedicated full screen

---

## Dependencies (add to build.gradle.kts)

```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Room (local DB = Hive equivalent)
implementation("androidx.room:room-runtime:2.7.1")
implementation("androidx.room:room-ktx:2.7.1")
ksp("androidx.room:room-compiler:2.7.1")

// Hilt (dependency injection = get_it equivalent)
implementation("com.google.dagger:hilt-android:2.51.1")
ksp("com.google.dagger:hilt-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Paging 3 (infinite scroll pagination)
implementation("androidx.paging:paging-runtime:3.3.6")
implementation("androidx.paging:paging-compose:3.3.6")

// Navigation Compose (= GoRouter)
implementation("androidx.navigation:navigation-compose:2.9.0")

// Coil (image loading + caching = cached_network_image)
implementation("io.coil-kt:coil-compose:2.7.0")

// ViewModel + Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")

// Kotlin Coroutines (= Dart async/await, but more powerful)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
```

---

## Build Order (Day-by-Day Plan)

### Day 1 — Foundation + Data
1. Add all dependencies, configure Hilt
2. Set up TMDB API key via `BuildConfig`
3. Build `TmdbApi.kt` (Retrofit interface with all endpoints)
4. Build domain models + repository interfaces
5. Build `MovieRepositoryImpl` (remote only)
6. Build `HomeViewModel` + `HomeScreen` with trending feed + pagination
7. Build `SearchViewModel` + `SearchScreen` with debounced search

### Day 2 — Detail + Watchlist + Polish
1. Build `DetailScreen` with full movie info + cast
2. Build `Room` setup (DB, DAO, entity)
3. Build `WatchlistRepository` + watchlist use cases
4. Wire watchlist toggle on Detail screen
5. Build `WatchlistScreen`
6. Add reviews section to Detail
7. Polish: loading states, error handling, empty states, nav transitions

---

## TMDB Endpoints Used

| Feature | Endpoint |
|---|---|
| Trending movies | `GET /trending/movie/day` |
| Trending shows | `GET /trending/tv/day` |
| Popular movies | `GET /movie/popular` |
| Search movies | `GET /search/movie?query=` |
| Search shows | `GET /search/tv?query=` |
| Movie detail | `GET /movie/{id}` |
| Movie cast | `GET /movie/{id}/credits` |
| Movie reviews | `GET /movie/{id}/reviews` |
| Show detail | `GET /tv/{id}` |
| Image base URL | `https://image.tmdb.org/t/p/w500{poster_path}` |

---

## What to Tell the Internship

- **Architecture pattern:** Clean Architecture + MVVM
- **Key libraries:** Hilt, Retrofit, Room, Paging 3, Jetpack Compose, Coil
- **Patterns demonstrated:** Repository pattern, Use Cases, PagingSource, StateFlow, Coroutines, DI
- **Offline support:** Watchlist persisted with Room, survives app restarts
