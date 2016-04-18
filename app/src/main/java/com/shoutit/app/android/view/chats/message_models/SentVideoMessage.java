package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class SentVideoMessage implements BaseAdapterItem {

    private final String videoThumbnail;
    private final String time;
    private final Listener mListener;
    private final String url;

    public SentVideoMessage(String videoThumbnail, String time, Listener listener, String url) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SentVideoMessage that = (SentVideoMessage) o;

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
        return item instanceof SentVideoMessage && time.equals(((SentVideoMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof SentVideoMessage && this.equals(item);
    }

    public void click() {
        mListener.onVideoClicked(url);
    }
}