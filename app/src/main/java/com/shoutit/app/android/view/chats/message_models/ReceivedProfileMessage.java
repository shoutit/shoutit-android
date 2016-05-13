package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class ReceivedProfileMessage extends ReceivedMessage {

    private final String time;
    private final String id;
    private final String username;
    private final String name;
    private final String image;
    private final String cover;
    private final int listenersCount;
    private final Listener listener;

    public ReceivedProfileMessage(boolean isFirst, String time, String avatarUrl, String id, String username, String name,
                                  String image, String cover, int listenersCount, Listener listener) {
        super(isFirst, avatarUrl);
        this.time = time;
        this.id = id;
        this.username = username;
        this.name = name;
        this.image = image;
        this.cover = cover;
        this.listenersCount = listenersCount;
        this.listener = listener;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getListenersCount() {
        return listenersCount;
    }

    public String getImage() {
        return image;
    }

    public void click(){
        listener.onProfileClicked(username);
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedProfileMessage
                && id.equals(((ReceivedProfileMessage) item).id);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof ReceivedProfileMessage &&
                this.equals(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceivedProfileMessage)) return false;
        final ReceivedProfileMessage that = (ReceivedProfileMessage) o;
        return listenersCount == that.listenersCount &&
                Objects.equal(time, that.time) &&
                Objects.equal(id, that.id) &&
                Objects.equal(username, that.username) &&
                Objects.equal(name, that.name) &&
                Objects.equal(image, that.image) &&
                Objects.equal(cover, that.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(time, id, username, name, image, cover, listenersCount);
    }
}
