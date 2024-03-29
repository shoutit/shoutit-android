package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedVideoMessage extends ReceivedMessage {

    private final String videoThumbnail;
    private final String time;
    @Nonnull
    private final String userName;
    private final Listener mListener;
    private final String url;
    private final boolean mIsPage;

    public ReceivedVideoMessage(boolean isFirst, String videoThumbnail, String time,
                                String avatarUrl, @Nonnull String userName, Listener listener,
                                String url, boolean isPage) {
        super(isFirst, avatarUrl);
        this.videoThumbnail = videoThumbnail;
        this.time = time;
        this.userName = userName;
        mListener = listener;
        this.url = url;
        mIsPage = isPage;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReceivedVideoMessage that = (ReceivedVideoMessage) o;

        if (videoThumbnail != null ? !videoThumbnail.equals(that.videoThumbnail) : that.videoThumbnail != null)
            return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (mListener != null ? !mListener.equals(that.mListener) : that.mListener != null)
            return false;
        return url != null ? url.equals(that.url) : that.url == null;

    }

    @Override
    public int hashCode() {
        int result = videoThumbnail != null ? videoThumbnail.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (mListener != null ? mListener.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedVideoMessage && time.equals(((ReceivedVideoMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedVideoMessage && this.equals(item);
    }

    public void click() {
        mListener.onVideoClicked(url);
    }

    public void onAvatarClicked() {
        mListener.onProfileClicked(userName, mIsPage);
    }
}