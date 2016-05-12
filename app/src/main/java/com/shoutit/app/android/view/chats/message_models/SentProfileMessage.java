package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.google.common.base.Objects;
import com.shoutit.app.android.adapteritems.BaseNoIDAdapterItem;
import com.shoutit.app.android.view.chats.Listener;

import javax.annotation.Nonnull;

public class SentProfileMessage extends BaseNoIDAdapterItem {

    private final String time;
    @Nonnull
    private final String id;
    @Nonnull
    private final String username;
    @Nonnull
    private final String name;
    private final String image;
    private final String cover;
    private final int listenersCount;
    private final Listener listener;

    public SentProfileMessage(String time, @Nonnull String id, @Nonnull String username, @Nonnull String name,
                              String image, String cover, int listenersCount, Listener listener) {
        this.time = time;
        this.id = id;
        this.username = username;
        this.name = name;
        this.image = image;
        this.cover = cover;
        this.listenersCount = listenersCount;
        this.listener = listener;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public int getListenersCount() {
        return listenersCount;
    }

    public String getTime() {
        return time;
    }

    public void click(){
        listener.onProfileClicked(username);
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof SentProfileMessage
                && id.equals(((SentProfileMessage) item).id);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof SentProfileMessage &&
                this.equals(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SentProfileMessage)) return false;
        final SentProfileMessage that = (SentProfileMessage) o;
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
