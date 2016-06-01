package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class MutualFriendsResponse extends PaginatedResponse {

    @Nonnull
    private final List<BaseProfile> results;

    public MutualFriendsResponse(int count, @Nullable String next, @Nullable String previous,
                                 @Nonnull List<BaseProfile> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nonnull
    public List<BaseProfile> getResults() {
        return results;
    }
}
