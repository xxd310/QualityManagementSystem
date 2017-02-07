package com.zhihuitech.qualitymanagementsystem;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class PlayVideoActivity extends Activity {
    private ImageView ivBack;
    private VideoView videoView;
    private TextView tvPath;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_video);

        Intent intent = getIntent();
        videoUrl = intent.getStringExtra("url");

        findViews();

        tvPath.setText(videoUrl);
        videoView.setVideoURI(Uri.parse(videoUrl));
        MediaController mediaController = new MediaController(this);
        System.out.println("----" + videoView.getDuration());
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.requestFocusFromTouch();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_play_video);
        videoView = (VideoView) findViewById(R.id.vv_play_video);
        tvPath = (TextView) findViewById(R.id.tv_path_play_video);
    }
}
