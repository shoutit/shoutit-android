package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class PagesResponse extends PaginatedResponse {

    @Nonnull
    private final List<Page> results;

    public PagesResponse(int count, @Nullable String next, @Nullable String previous, @Nonnull List<Page> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nonnull
    public List<Page> getResults() {
        return results;
    }
}