package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;

public class ShoutsResponse extends PaginatedResponse {
    @Nonnull
    final List<Shout> results;

    public ShoutsResponse(@Nonnull List<Shout> results) {
        this.results = results;
    }

    @Nonnull
    public List<Shout> getShouts() {
        return results;
    }
}
