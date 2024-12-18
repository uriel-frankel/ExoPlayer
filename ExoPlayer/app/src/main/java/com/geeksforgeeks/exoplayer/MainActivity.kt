package com.geeksforgeeks.exoplayer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
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

        // Build the MediaItem


        if (!folderExists(this, "parts")) {
            Toast.makeText(this, "Folder doesn't exists. Copying files to filesDir", Toast.LENGTH_SHORT).show()
            CoroutineScope(IO).launch {
                assets.list("")?.forEach {
                    copyAssetToFileDir(this@MainActivity, it, "parts")
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "done", Toast.LENGTH_SHORT).show()
                    playFiles()
                }
            }
        } else {
            playFiles()
        }


    }

    @OptIn(UnstableApi::class)
    private fun playFiles() {
        val audioParts = ArrayList<File>()
        val videoParts = ArrayList<File>()

        val folder = File(filesDir, "parts")

        folder.listFiles()?.forEach {
            if (it.absolutePath.endsWith(".mp4")) {
                videoParts.add(it)
            } else {
                audioParts.add(it)
            }
        }

        Log.d(TAG, "videoParts size ${videoParts.size} or audioParts ${audioParts.size}")
        if (videoParts.isEmpty() || audioParts.isEmpty()) {
            // maybe add here some error handling
            Log.d(TAG, "showPreview: videoParts or audioParts is empty. videoParts size ${videoParts.size} or audioParts ${audioParts.size}")
            return
        }
        Log.d(TAG, "showPreview: videoParts size ${videoParts.size} or audioParts ${audioParts.size}")
        if (videoParts.size != audioParts.size) {
            Log.d(TAG, "showPreview: videoParts and audioParts are not the same size. doing hack to make the same size")
            if (videoParts.size > audioParts.size) {
                val diff = videoParts.size - audioParts.size
                for (i in 0 until diff) {
                    audioParts.add(audioParts.last())
                }
            } else {
                val diff = audioParts.size - videoParts.size
                for (i in 0 until diff) {
                    videoParts.add(videoParts.last())
                }
            }
        }
        // Prepare the player with the media item
        val videoMediaSource = generateMediaSource(videoParts)
        val audioMediaSource = generateMediaSource(audioParts)
        val mergedMediaSource = MergingMediaSource(true, true, videoMediaSource, audioMediaSource)

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


    @OptIn(UnstableApi::class)
    private fun generateMediaSource(files: List<File>): ConcatenatingMediaSource2 {
        var timeOfPart = 0L
        val builder = ConcatenatingMediaSource2.Builder().setMediaSourceFactory(DefaultMediaSourceFactory(FileDataSource.Factory()))
        files.forEach {
            val mediaItem = MediaItem.Builder().setUri(Uri.fromFile(File(it.path)))
            if(it.absolutePath.endsWith(".mp4")) {
                mediaItem.setMimeType("video/mp4")
                mediaItem.setClipStartsAtKeyFrame(true)
            } else {
                mediaItem.setMimeType("audio/mp4")
            }
            val duration = (extractNumberFromFilename(it.absolutePath) ?: 0) / 1000L
            timeOfPart += duration
            builder.add(mediaItem.build(), duration)
        }
        Log.d(TAG, "generateMediaSource: timeOfPart: $timeOfPart")
        return builder.build()
    }

    private fun extractNumberFromFilename(filename: String): Int? {
        // Find the positions of the last "_" and the last "."
        val lastUnderscoreIndex = filename.lastIndexOf('_')
        val lastDotIndex = filename.lastIndexOf('.')

        // Ensure both symbols exist and "_" comes before "."
        if (lastUnderscoreIndex == -1 || lastDotIndex == -1 || lastUnderscoreIndex > lastDotIndex) {
            println("Invalid filename format.")
            return null
        }

        // Extract the substring between "_" and "."
        val numberSubstring = filename.substring(lastUnderscoreIndex + 1, lastDotIndex)

        // Convert the substring to an integer
        return try {
            numberSubstring.toInt()
        } catch (e: NumberFormatException) {
            println("Failed to parse the number: $numberSubstring")
            null
        }
    }


    fun copyAssetToFileDir(context: Context, assetFileName: String, folderName: String) {
        val assetManager = context.assets

        // Create the folder inside filesDir if it doesn't exist
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // Define the destination file inside the folder
        val destinationFile = File(folder, assetFileName)

        try {
            assetManager.open(assetFileName).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush() // Ensure data is fully written
                    println("File copied to: ${destinationFile.absolutePath}")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            println("Failed to copy asset file: $e")
        }
    }

    fun folderExists(context: Context, folderName: String): Boolean {
        val folder = File(context.filesDir, folderName)
        return folder.exists() && folder.isDirectory
    }

}
