package com.example.movietracker.presentation.navigation

// Sealed class = a closed set of subtypes. At compile time, Kotlin knows every possible Screen.
// Flutter equivalent: your GoRouter route constants or an enum of named routes.
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Watchlist : Screen("watchlist")

    // Detail carries arguments in the route path (like GoRouter's :id param)
    object Detail : Screen("detail/{movieId}/{mediaType}") {
        fun createRoute(movieId: Int, mediaType: String) = "detail/$movieId/$mediaType"
    }
}
