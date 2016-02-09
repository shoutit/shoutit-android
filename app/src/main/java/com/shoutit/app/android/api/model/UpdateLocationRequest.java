package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class UpdateLocationRequest {
    @Nonnull
    private final Location location;

    public UpdateLocationRequest(@Nonnull Location location) {
        this.location = location;
    }
}
