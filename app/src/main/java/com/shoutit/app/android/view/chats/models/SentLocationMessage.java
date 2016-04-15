package com.shoutit.app.android.view.chats.models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class SentLocationMessage implements BaseAdapterItem {

    private final String time;
    private final Listener mListener;
    private final double latitude;
    private final double longitude;

    public SentLocationMessage(String time, Listener listener, double latitude, double longitude) {
        this.time = time;
        mListener = listener;
        this.latitude = latitude;
        this.longitude = longitude;
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
        mListener.onLocationClicked(latitude, longitude);
    }
}