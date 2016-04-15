package com.shoutit.app.android.view.chats.models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedVideoMessage extends ReceivedMessage {

    private final String videoThumbnail;
    private final String time;
    private final Listener mListener;
    private final String url;

    public ReceivedVideoMessage(boolean isFirst, String videoThumbnail, String time, String avatarUrl, Listener listener, String url) {
        super(isFirst, avatarUrl);
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