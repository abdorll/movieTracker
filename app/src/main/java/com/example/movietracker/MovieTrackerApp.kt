package com.example.movietracker

import android.app.Application
import com.example.movietracker.di.AppContainer

class MovieTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
