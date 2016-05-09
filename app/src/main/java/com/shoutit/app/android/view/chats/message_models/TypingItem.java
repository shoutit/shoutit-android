package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class TypingItem implements BaseAdapterItem {

    private final String username;

    public TypingItem(String username) {
        this.username = username;
    }

    private String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypingItem that = (TypingItem) o;

        return username != null ? username.equals(that.username) : that.username == null;

    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    @Override
    public long adapterId() {
        return 0;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof TypingItem && equals(item);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof TypingItem && equals(item);
    }
}
