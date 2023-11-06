package com.couchbase.userprofile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Play_video extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        Intent intent = getIntent();
        String videoUriString = intent.getStringExtra("VideoUri");
        VideoView videoView = findViewById(R.id.video);


            // 使用 VideoView来播放
        if (videoUriString != null && !videoUriString.isEmpty()) {
            Uri videoUri = Uri.parse(videoUriString);

            videoView.setVideoURI(videoUri);
            videoView.setMediaController(new MediaController(this));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
                });

        }
        Button playButton = findViewById(R.id.playbutton);
        Button stopButton = findViewById(R.id.stopbutton);

        // 播放按鈕監聽器
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoView.isPlaying()) {
                    videoView.start();
                }
            }
        });

        // 停止按鈕監聽器
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                }
            }
        });
    }
}