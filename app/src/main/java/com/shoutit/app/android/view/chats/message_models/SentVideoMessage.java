package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class SentVideoMessage implements BaseAdapterItem {

    private final String videoThumbnail;
    private final String time;
    private final Listener mListener;
    private final String url;

    public SentVideoMessage(String videoThumbnail, String time, Listener listener, String url) {
        this.videoThumbnail = videoThumbnail;
        this.time = time;
        mListener = listener;
        this.url = url;
    }

    public String getVideoThumbnail() {
        return videoThumbnail;
    }

    public String getTime() {
        return time;
    }

    @Override
    public long adapterId() {
        return 0;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return false;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return false;
    }

    public void click() {
        mListener.onVideoClicked(url);
    }
}