package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class ReceivedTextMessage extends ReceivedMessage {

    private final String time;
    private final String message;

    public ReceivedTextMessage(boolean isFirst, String time, String message, String avatarUrl) {
        super(isFirst, avatarUrl);
        this.time = time;
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
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