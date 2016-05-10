package com.shoutit.app.android.view.shout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.shoutit.app.android.R;
import com.shoutit.app.android.utils.LogHelper;

import javax.annotation.Nonnull;

public class VideoPlayerActivity extends Activity {

    private static final String EXTRA_VIDEO_URL = "extra_video_url";

    public static Intent newIntent(@Nonnull Context context,
                                   @Nonnull String videoUrl) {
        return new Intent(context, VideoPlayerActivity.class)
                .putExtra(EXTRA_VIDEO_URL, videoUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String url = getIntent().getExtras().getString(EXTRA_VIDEO_URL);

        setContentView(R.layout.video_fragment);

        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                View progress = findViewById(R.id.videoProgress);
                progress.setVisibility(View.GONE);

                videoView.requestFocus();
                MediaController vidControl = new MediaController(VideoPlayerActivity.this);
                vidControl.setAnchorView(videoView);
                videoView.setMediaController(vidControl);
                videoView.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        videoView.setVideoURI(Uri.parse(url));
    }
}
