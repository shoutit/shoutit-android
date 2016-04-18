package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedLocationMessage extends ReceivedMessage {

    private final String time;
    private final Listener mListener;
    private final double latitude;
    private final double longitude;

    public ReceivedLocationMessage(boolean isFirst, String time, String avatarUrl, Listener listener, double latitude, double longitude) {
        super(isFirst, avatarUrl);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReceivedLocationMessage that = (ReceivedLocationMessage) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        return mListener != null ? mListener.equals(that.mListener) : that.mListener == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = time != null ? time.hashCode() : 0;
        result = 31 * result + (mListener != null ? mListener.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }


    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedLocationMessage && time.equals(((ReceivedLocationMessage) item).time);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedLocationMessage && this.equals(item);
    }

    public void click() {
        mListener.onLocationClicked(latitude, longitude);
    }
}