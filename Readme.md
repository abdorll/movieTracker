# MovieTracker

A polished Android app for discovering trending movies and TV shows, watching trailers, reading reviews, and building a personal watchlist — built entirely in Kotlin and Jetpack Compose.

This was built as an internship portfolio project while learning Android development from scratch, coming from a Flutter background.

---

## What the app does

**Home** — Shows trending movies and TV shows fetched from TMDB. Two tabs (Movies / TV Shows) let you switch between them. The grid loads more as you scroll (infinite pagination), and you can pull down to refresh.

**Search** — A pill-shaped search bar lets you search across both movies and TV shows at once. Results paginate as you scroll. Clearing the query brings back the "discover" prompt.

**Detail** — Tap any movie or show to get the full picture: backdrop image, title, tagline, star rating, vote count, runtime, genre chips, a full overview, a horizontally scrolling cast row, and user reviews. If a trailer exists, a play button appears over the backdrop — tap it to watch the trailer inline in the app. A heart FAB in the corner adds or removes the item from your watchlist.

**Watchlist** — Everything you've saved, persisted locally so it survives the app being closed. Filter chips let you view All / Movies / TV Shows only. Tap any item to go back to its detail screen.

**Light & Dark mode** — A toggle button in the top-right of the Home screen switches between a deep cinematic dark theme and a clean light theme. The choice persists across navigation within the session.

---

## Screens at a glance

| Screen | What's on it |
|---|---|
| Home | Trending grid, Movies/TV tabs, pull-to-refresh, theme toggle |
| Search | Live search with pagination, clear button, empty state |
| Detail | Full movie info, trailer player, cast, reviews, watchlist FAB |
| Watchlist | Saved items, filter by type, persisted locally |

---

## Architecture

The app follows **Clean Architecture** with three layers, plus a presentation layer:

```
app/
└── src/main/java/com/example/movietracker/
    │
    ├── data/                        ← talks to the outside world
    │   ├── remote/
    │   │   ├── api/TmdbApi.kt       ← Retrofit interface for TMDB endpoints
    │   │   ├── dto/                 ← raw JSON shapes (MovieDto, MovieDetailDto, etc.)
    │   │   └── interceptor/         ← attaches the API key to every request
    │   ├── local/
    │   │   ├── MovieDatabase.kt     ← Room database
    │   │   ├── dao/WatchlistDao.kt  ← SQL queries for watchlist
    │   │   └── entity/              ← what gets stored in the database
    │   ├── paging/                  ← PagingSource classes for infinite scroll
    │   ├── mapper/                  ← converts DTOs ↔ domain models
    │   └── repository/              ← implements the domain repository interfaces
    │
    ├── domain/                      ← pure business logic, no Android imports
    │   ├── model/                   ← Movie, MovieDetail, CastMember, Review
    │   ├── repository/              ← interfaces (contracts) for data access
    │   └── usecase/                 ← one class per action (GetTrending, Search, etc.)
    │
    ├── presentation/                ← everything the user sees
    │   ├── screens/
    │   │   ├── home/                ← HomeScreen + HomeViewModel
    │   │   ├── search/              ← SearchScreen + SearchViewModel
    │   │   ├── detail/              ← DetailScreen + DetailViewModel + DetailUiState
    │   │   └── watchlist/           ← WatchlistScreen + WatchlistViewModel
    │   ├── components/              ← reusable composables
    │   │   ├── MovieCard.kt         ← full-bleed poster card with gradient + badge
    │   │   ├── MovieTrackerLogo.kt  ← the MOVIE / TRACKER wordmark
    │   │   ├── YoutubePlayer.kt     ← in-app YouTube player (androidyoutubeplayer)
    │   │   ├── RatingBar.kt         ← star + score
    │   │   ├── PosterImage.kt       ← Coil image with placeholder
    │   │   ├── LoadingIndicator.kt  ← centered spinner
    │   │   └── ErrorView.kt         ← error message + retry button
    │   └── navigation/
    │       ├── NavGraph.kt          ← navigation graph + floating bottom bar
    │       └── Screen.kt            ← route definitions
    │
    ├── di/AppContainer.kt           ← manual dependency injection (no Hilt)
    ├── ui/theme/                    ← Color, Type, Theme (cinematic dark/light palette)
    └── util/                        ← Constants, Resource sealed class, extensions
```

---

## Tech stack

| What | Library / Tool |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Networking | Retrofit 2 + OkHttp |
| Image loading | Coil |
| Local database | Room |
| Infinite scroll | Paging 3 |
| In-app video | androidyoutubeplayer |
| Async | Kotlin Coroutines + Flow |
| Dependency injection | Manual (AppContainer object) |
| Splash screen | AndroidX SplashScreen API |
| Build system | Gradle (Kotlin DSL) + KSP |

---

## Design

The app uses a custom cinematic design system built from scratch:

- **Dark theme** — deep midnight blue (`#0B0C14`) background, layered surface tones, amber gold primary (`#F0A500`)
- **Light theme** — clean off-white background, dark amber primary (`#7A5000`)
- **Typography** — Serif for headlines (cinematic feel), SansSerif for body and labels
- **Movie cards** — full-bleed poster with a gradient scrim over the bottom third, white text on top, media type badge (Film / TV) in the corner
- **Bottom navigation** — floating pill card with rounded corners, elevation shadow, gold indicator for the selected tab
- **Logo** — two-line wordmark: "MOVIE" in gold serif with wide letter-spacing, "TRACKER" in light grey below

---

## API key setup

The app uses the [TMDB API](https://www.themoviedb.org/documentation/api). The key is **never** committed to the repo. To run the project:

1. Get a free API key from [themoviedb.org](https://www.themoviedb.org/settings/api)
2. Create a `local.properties` file in the project root (this file is git-ignored)
3. Add this line:

```
TMDB_API_KEY=your_key_here
```

The key is injected at build time via `BuildConfig.TMDB_API_KEY` and attached to every network request by `AuthInterceptor`.

---

## Running the project

```bash
# debug build
./gradlew assembleDebug

# install directly to a connected phone (USB debugging on)
./gradlew installDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

Minimum Android version: **API 24 (Android 7.0)**  
Target SDK: **API 36**
