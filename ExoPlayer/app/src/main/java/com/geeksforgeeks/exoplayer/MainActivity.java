package com.geeksforgeeks.exoplayer;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;


public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        playerView = findViewById(R.id.player_view);

        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Build the MediaItem
        String videoUrl = "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4";
        Uri uri = Uri.parse(videoUrl);
        MediaItem mediaItem = MediaItem.fromUri(uri);

        // Prepare the player with the media item
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true); // Start playing when ready
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
