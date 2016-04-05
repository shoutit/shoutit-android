package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class SentVideoMessage implements BaseAdapterItem {

    private final String videoThumbnail;
    private final String time;

    public SentVideoMessage(String videoThumbnail, String time) {
        this.videoThumbnail = videoThumbnail;
        this.time = time;
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

}