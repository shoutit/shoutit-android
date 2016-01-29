package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class DiscoverResponse extends PaginatedResponse {

    @Nonnull
    private final List<Discover> results;

    public DiscoverResponse(int count, @Nullable String next, @Nullable String previous,
                            @Nonnull List<Discover> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nullable
    public List<Discover> getDiscovers() {
        return results;
    }
}
