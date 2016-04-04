package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class SentImageMessage implements BaseAdapterItem {

    private final String time;
    private final String url;

    public SentImageMessage(String time, String url) {
        super();
        this.time = time;
        this.url = url;
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

}