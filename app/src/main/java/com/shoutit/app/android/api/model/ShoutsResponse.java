package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class ShoutsResponse extends PaginatedResponse {
    @Nonnull
    final List<Shout> results;

    public ShoutsResponse(int count, String next, String previous, @Nonnull List<Shout> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nullable
    public List<Shout> getShouts() {
        return results;
    }
}
