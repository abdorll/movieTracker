package com.example.movietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.movietracker.presentation.navigation.NavGraph
import com.example.movietracker.ui.theme.MovieTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            // null = follow system, true/false = user override
            var themeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
            val isDark = themeOverride ?: systemDark

            MovieTrackerTheme(darkTheme = isDark) {
                NavGraph(
                    isDark = isDark,
                    onToggleTheme = { themeOverride = !isDark }
                )
            }
        }
    }
}
