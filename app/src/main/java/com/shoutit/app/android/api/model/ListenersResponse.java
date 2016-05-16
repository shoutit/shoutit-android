package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ListenersResponse extends PaginatedResponse {

    @Nonnull
    private final List<BaseProfile> results;

    public ListenersResponse(int count, @Nullable String next, @Nullable String previous,
                             @Nonnull List<BaseProfile> results) {
        super(count, next, previous);
        this.results = results;
    }

    @Nonnull
    public List<BaseProfile> getProfiles() {
        return results;
    }
}

