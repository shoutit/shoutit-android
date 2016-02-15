package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

public class ShoutsResponse extends PaginatedResponse {

    @Nullable
    final List<Shout> results;

    public ShoutsResponse(int count, String next, String previous, @Nullable List<Shout> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nullable
    public List<Shout> getShouts() {
        return results;
    }
}
