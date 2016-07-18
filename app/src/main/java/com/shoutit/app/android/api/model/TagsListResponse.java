package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class TagsListResponse extends PaginatedResponse {

    @Nonnull
    private final List<TagDetail> results;

    public TagsListResponse(int count, @Nullable String next, @Nullable String previous,
                                @Nonnull List<TagDetail> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nonnull
    public List<TagDetail> getResults() {
        return results;
    }
}
