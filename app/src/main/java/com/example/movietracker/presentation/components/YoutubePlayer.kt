package com.example.movietracker.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YoutubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            YouTubePlayerView(context).apply {
                // Attaching to the lifecycle means the player automatically pauses when
                // the user backgrounds the app and resumes when they come back.
                lifecycleOwner.lifecycle.addObserver(this)
                enableBackgroundPlayback(false)

                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // loadVideo starts playback immediately once the player is ready.
                        youTubePlayer.loadVideo(videoId, 0f)
                    }

                    override fun onError(
                        youTubePlayer: YouTubePlayer,
                        error: PlayerConstants.PlayerError
                    ) {
                        // Error 150/152 means the video owner disabled embedding.
                        // Signal the caller so it can open YouTube externally instead.
                        onError()
                    }
                })
            }
        },
        modifier = modifier
    )
}
