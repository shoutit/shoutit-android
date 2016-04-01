package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class ReceivedLocationMessage extends ReceivedMessage {

    private final String time;

    public ReceivedLocationMessage(boolean isFirst, String time) {
        super(isFirst);
        this.time = time;
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