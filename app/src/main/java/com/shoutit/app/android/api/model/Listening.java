package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

public class Listening {

    private final int pages;
    private final int users;
    private final int tags;

    public Listening(int pages, int users, int tags) {
        this.pages = pages;
        this.users = users;
        this.tags = tags;
    }

    public int getProfileListening() {
        return pages + users;
    }

    public int getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Listening)) return false;
        final Listening listening = (Listening) o;
        return pages == listening.pages &&
                users == listening.users &&
                tags == listening.tags;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pages, users, tags);
    }
}
