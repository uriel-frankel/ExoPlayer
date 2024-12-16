package com.geeksforgeeks.exoplayer

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.CreationExtras.Empty.map
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView


class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        playerView = findViewById(R.id.player_view)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView?.setPlayer(player)

        val list = arrayListOf(
            "asset:///a01.mp4",
            "asset:///v01.mp4",
            "asset:///a02.mp4",
            "asset:///v02.mp4",
            "asset:///a03.mp4",
            "asset:///v03.mp4",
            "asset:///a04.mp4",
            "asset:///v04.mp4",
        )
        // Build the MediaItem


        // Prepare the player with the media item
        val items = list.map {ProgressiveMediaSource.Factory(FileDataSource.Factory()).createMediaSource(MediaItem.fromUri(it))}

        val i = items.toTypedArray()
        val mediaSource = MergingMediaSource(false, *i)
        player!!.setMediaSource(mediaSource)
        player!!.prepare()
        player!!.playWhenReady = true // Start playing when ready
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.release()
            player = null
        }
    }
}
