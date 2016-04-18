package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class SentImageMessage implements BaseAdapterItem {

    private final String time;
    private final String url;
    private final Listener mListener;

    public SentImageMessage(String time, String url, Listener listener) {
        super();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SentImageMessage that = (SentImageMessage) o;

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
        return item instanceof SentImageMessage && time.equals(((SentImageMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof SentImageMessage && this.equals(item);
    }

    public void click() {
        mListener.onImageClicked(url);
    }
}