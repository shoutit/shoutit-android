package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class UpdateLocationRequest {
    @Nonnull
    private final UserLocation location;

    public UpdateLocationRequest(@Nonnull UserLocation location) {
        this.location = location;
    }
}
