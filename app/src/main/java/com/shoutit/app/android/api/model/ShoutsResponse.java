package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import java.util.List;

public class ShoutsResponse extends PaginatedResponse {

    @NonNull
    final List<Shout> results;

    public ShoutsResponse(int count, String next, String previous, @NonNull List<Shout> results) {
        super(count, next, previous);
        this.results = results;
    }

    @NonNull
    public List<Shout> getShouts() {
        return results;
    }
}
