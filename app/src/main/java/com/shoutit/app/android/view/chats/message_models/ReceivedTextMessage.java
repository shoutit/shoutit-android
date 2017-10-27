package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

import rx.Observer;

public class ReceivedTextMessage extends ReceivedMessage {

    private final String time;
    private final String message;
    @Nonnull
    private final String userName;
    @Nonnull
    private final Listener listener;
    private final boolean mIsPage;

    public ReceivedTextMessage(boolean isFirst, String time, String message, String avatarUrl,
                               @Nonnull String userName, @Nonnull Listener listener, boolean isPage) {
        super(isFirst, avatarUrl);
        this.time = time;
        this.message = message;
        this.userName = userName;
        this.listener = listener;
        mIsPage = isPage;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReceivedTextMessage that = (ReceivedTextMessage) o;

        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        return message != null ? message.equals(that.message) : that.message == null;

    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedTextMessage && time.equals(((ReceivedTextMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedTextMessage && this.equals(item);
    }

    public void onAvatarClicked() {
        listener.onProfileClicked(userName, mIsPage);
    }
}