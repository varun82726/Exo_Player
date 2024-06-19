package com.ramdisk.exo_player

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView

lateinit var player: Player
private val TAG = "varun"

@OptIn(UnstableApi::class)
@Composable
fun IndependentExoPlayer(videoUri: String, playerActivity: PlayerActivity) {

    val context = LocalContext.current
    val mediaSourceFactory: MediaSource.Factory =
        DefaultMediaSourceFactory(context) // for online videos you need to implement some extra dependencies
    player = ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build().apply {
        setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
    }
    val playerView = PlayerView(context)
    val playWhenReady by rememberSaveable { mutableStateOf(true) }
    playerView.player = player
    playerView.useController = true


    // Obtain the lifecycle owner
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a remember observer
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    // Handle onCreate
                    Log.d(TAG, "ON_CREATE called")
                }

                Lifecycle.Event.ON_START -> {
                    // Handle onStart
                    Log.d(TAG, "ON_START called")
                }

                Lifecycle.Event.ON_RESUME -> {
                    // Handle onResume
                    Log.d(TAG, "ON_RESUME called")
                    player.play()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    // Handle onPause
                    Log.d(TAG, "ON_PAUSE called")
                    player.pause()
                }

                Lifecycle.Event.ON_STOP -> {
                    // Handle onStop
                    Log.d(TAG, "ON_STOP called")
                }

                Lifecycle.Event.ON_DESTROY -> {
                    // Handle onDestroy
                    Log.d(TAG, "ON_DESTROY called")
                    player.release()
                }

                else -> {}
            }
        }
    }


    @OptIn(UnstableApi::class)
    val playerListener =
        remember {
            object : Player.Listener {
                override fun onTracksChanged(tracks: Tracks) {
                    super.onTracksChanged(tracks)
                    if (tracks != null && tracks.groups != null) {
                        for (group in tracks.groups) {
                            for (i in 0 until group.mediaTrackGroup.length) {
                                val format = group.mediaTrackGroup.getFormat(i)
                                when (group.type) {
                                    2 -> {
                                        //video width
                                        if (format != null) {
                                            if (group.isTrackSelected(i)) {
                                                if (format.height < format.width) {//LandScape
                                                    playerActivity.ChangeOrientationToLandScape()
                                                    Log.d(TAG, "onTracksChanged: Landscape")
                                                } else {
                                                    playerActivity.ChangeOrientationToPortrait()
                                                    Log.d(TAG, "onTracksChanged: Portrait")
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

    player.addListener(playerListener)


    // Attach the observer to the lifecycle
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    LaunchedEffect(key1 = player) {
        player.prepare()
        player.playWhenReady = playWhenReady
    }


    AndroidView(
        factory = { playerView },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )

}



