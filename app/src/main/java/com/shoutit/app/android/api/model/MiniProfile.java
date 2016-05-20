package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class MiniProfile {
    @Nonnull
    private final String id;
    @Nonnull
    private final String username;
    @Nonnull
    private final String name;

    public MiniProfile(@Nonnull String id, @Nonnull String username, @Nonnull String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    @Nonnull
    public String getName() {
        return name;
    }
}
