package com.geeksforgeeks.exoplayer

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.ui.PlayerView
import java.io.File

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

        val videoList = arrayListOf(
            "v01.mp4",
            "v02.mp4",
            "v03.mp4",
            "v04.mp4",
        )

        // Build the MediaItem


        // Prepare the player with the media item
        val videoMediaItems = videoList.map { MediaItem.fromUri(Uri.fromFile(File(MainActivity@ this.filesDir.absolutePath + File.separator + it))) }
        val videoBuilder = ConcatenatingMediaSource2.Builder().setMediaSourceFactory(DefaultMediaSourceFactory(FileDataSource.Factory()))
        videoMediaItems.forEach { videoBuilder.add(it, 5000) }
        val videoMediaSource = videoBuilder.build()



        val audioList = arrayListOf(
            "a01.mp4",
            "a02.mp4",
            "a03.mp4",
            "a04.mp4",
        )

        val audioMediaItems = audioList.map { MediaItem.fromUri(Uri.fromFile(File(MainActivity@ this.filesDir.absolutePath + File.separator + it))) }
        val audioBuilder = ConcatenatingMediaSource2.Builder().setMediaSourceFactory(DefaultMediaSourceFactory(FileDataSource.Factory()))
        audioMediaItems.forEach { audioBuilder.add(it, 5000) }
        val audioMediaSource = audioBuilder.build()

        val mergedMediaSource = MergingMediaSource(videoMediaSource, audioMediaSource)

        player!!.setMediaSource(mergedMediaSource)
        player!!.prepare()
        player!!.playWhenReady = true // Start

    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.release()
            player = null
        }
    }
}
