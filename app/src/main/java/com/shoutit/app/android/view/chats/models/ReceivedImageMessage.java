package com.shoutit.app.android.view.chats.models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedImageMessage extends ReceivedMessage {

    private final String time;
    private final String url;
    private final Listener mListener;

    public ReceivedImageMessage(boolean isFirst, String time, String url, String avatarUrl, Listener listener) {
        super(isFirst, avatarUrl);
        this.time = time;
        this.url = url;
        mListener = listener;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
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
        mListener.onImageClicked(url);
    }
}