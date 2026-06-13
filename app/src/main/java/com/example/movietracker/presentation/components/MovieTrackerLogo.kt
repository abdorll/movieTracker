package com.example.movietracker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MovieTrackerLogo(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(start = 4.dp)) {
        Text(
            text = "MOVIE",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 6.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "TRACKER",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
