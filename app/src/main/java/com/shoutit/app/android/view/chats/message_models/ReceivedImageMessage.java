package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedImageMessage extends ReceivedMessage {

    private final String time;
    private final String url;
    private final String userName;
    private final Listener mListener;
    private final boolean mIsPage;

    public ReceivedImageMessage(boolean isFirst, String time, String url,
                                String avatarUrl, String userName, Listener listener, boolean isPage) {
        super(isFirst, avatarUrl);
        this.time = time;
        this.url = url;
        this.userName = userName;
        mListener = listener;
        mIsPage = isPage;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReceivedImageMessage that = (ReceivedImageMessage) o;

        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return mListener != null ? mListener.equals(that.mListener) : that.mListener == null;

    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (mListener != null ? mListener.hashCode() : 0);
        return result;
    }

    @Override
    public long adapterId() {
        return 0;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedImageMessage && time.equals(((ReceivedImageMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedImageMessage && this.equals(item);
    }

    public void click() {
        mListener.onImageClicked(url);
    }

    public void onAvatarClicked() {
        mListener.onProfileClicked(userName, mIsPage);
    }
}