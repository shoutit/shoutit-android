package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class MiniProfile {
    @Nonnull
    private final String id;
    @Nonnull
    private final String userName;
    @Nonnull
    private final String name;

    public MiniProfile(@Nonnull String id, @Nonnull String userName, @Nonnull String name) {
        this.id = id;
        this.userName = userName;
        this.name = name;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getUserName() {
        return userName;
    }

    @Nonnull
    public String getName() {
        return name;
    }
}
