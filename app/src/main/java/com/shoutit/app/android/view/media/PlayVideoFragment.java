package com.shoutit.app.android.view.media;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.shoutit.app.android.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlayVideoFragment extends Fragment {

    public static final String ARG_VIDEO_PATH = "video_path";

    @Bind(R.id.videoView)
    VideoView videoView;

    public static PlayVideoFragment newInstance(String videoPath) {
        Bundle bndl = new Bundle();
        bndl.putString(ARG_VIDEO_PATH, videoPath);
        PlayVideoFragment frag = new PlayVideoFragment();
        frag.setArguments(bndl);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        videoView.setVideoPath(getArguments().getString(ARG_VIDEO_PATH));
        final MediaController controller = new MediaController(getActivity());
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) controller.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.camera_accept_or_dismiss_layout_height);
        videoView.setMediaController(controller);

        videoView.start();
    }

}
