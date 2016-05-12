package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedShoutMessage extends ReceivedMessage {

    private final String shoutImageUrl;
    private final String time;
    private final String price;
    private final String description;
    private final String author;
    private final Listener mListener;
    private final String mShoutId;


    public ReceivedShoutMessage(boolean isFirst, String shoutImageUrl, String time, String price, String description, String author, String avatarUrl, Listener listener, String shoutId) {
        super(isFirst, avatarUrl);
        this.shoutImageUrl = shoutImageUrl;
        this.time = time;
        this.price = price;
        this.description = description;
        this.author = author;
        mListener = listener;
        mShoutId = shoutId;
    }

    public String getShoutImageUrl() {
        return shoutImageUrl;
    }

    public String getTime() {
        return time;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReceivedShoutMessage that = (ReceivedShoutMessage) o;

        if (shoutImageUrl != null ? !shoutImageUrl.equals(that.shoutImageUrl) : that.shoutImageUrl != null)
            return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (mListener != null ? !mListener.equals(that.mListener) : that.mListener != null)
            return false;
        return mShoutId != null ? mShoutId.equals(that.mShoutId) : that.mShoutId == null;

    }

    @Override
    public int hashCode() {
        int result = shoutImageUrl != null ? shoutImageUrl.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (mListener != null ? mListener.hashCode() : 0);
        result = 31 * result + (mShoutId != null ? mShoutId.hashCode() : 0);
        return result;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedShoutMessage && time.equals(((ReceivedShoutMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedShoutMessage && this.equals(item);
    }

    public void click(){
        mListener.onShoutClicked(mShoutId);
    }
}