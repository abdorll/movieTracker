package com.example.movietracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary               = GoldPrimary,
    onPrimary             = GoldOnPrimary,
    primaryContainer      = GoldPrimaryContainer,
    onPrimaryContainer    = GoldOnPrimaryContainer,
    secondary             = DarkSecondary,
    onSecondary           = DarkOnSecondary,
    secondaryContainer    = DarkSecondaryContainer,
    onSecondaryContainer  = DarkOnSecondaryContainer,
    tertiary              = DarkTertiary,
    onTertiary            = DarkOnTertiary,
    background            = DarkBackground,
    onBackground          = DarkOnBackground,
    surface               = DarkSurface,
    onSurface             = DarkOnSurface,
    surfaceVariant        = DarkSurfaceVariant,
    onSurfaceVariant      = DarkOnSurfaceVariant,
    surfaceContainer      = DarkSurfaceContainer,
    surfaceContainerHigh  = DarkSurfaceContainerHigh,
    error                 = DarkError,
    onError               = DarkOnError,
    errorContainer        = DarkErrorContainer,
    onErrorContainer      = DarkOnErrorContainer,
    outline               = DarkOutline,
    outlineVariant        = DarkOutlineVariant,
    inverseSurface        = DarkInverseSurface,
    inverseOnSurface      = DarkInverseOnSurface,
    inversePrimary        = DarkInversePrimary,
)

private val LightColorScheme = lightColorScheme(
    primary               = GoldPrimaryLight,
    onPrimary             = GoldOnPrimaryLight,
    primaryContainer      = GoldPrimaryContainerLight,
    onPrimaryContainer    = GoldOnPrimaryContainerLight,
    secondary             = LightSecondary,
    onSecondary           = LightOnSecondary,
    secondaryContainer    = LightSecondaryContainer,
    onSecondaryContainer  = LightOnSecondaryContainer,
    tertiary              = LightTertiary,
    onTertiary            = LightOnTertiary,
    background            = LightBackground,
    onBackground          = LightOnBackground,
    surface               = LightSurface,
    onSurface             = LightOnSurface,
    surfaceVariant        = LightSurfaceVariant,
    onSurfaceVariant      = LightOnSurfaceVariant,
    error                 = LightError,
    onError               = LightOnError,
    errorContainer        = LightErrorContainer,
    onErrorContainer      = LightOnErrorContainer,
    outline               = LightOutline,
    outlineVariant        = LightOutlineVariant,
    inverseSurface        = LightInverseSurface,
    inverseOnSurface      = LightInverseOnSurface,
    inversePrimary        = LightInversePrimary,
)

@Composable
fun MovieTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MovieTrackerTypography,
        content = content
    )
}
