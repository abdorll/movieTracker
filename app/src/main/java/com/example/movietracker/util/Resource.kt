package com.example.movietracker.util

// Flutter equivalent: sealed class + AsyncValue (Riverpod) or the result of a FutureBuilder
// Wraps every network/DB call so the UI always knows: am I loading? did it succeed? did it fail?
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    class Loading<T> : Resource<T>()
}
