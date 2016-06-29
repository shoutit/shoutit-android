package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import java.util.List;

import javax.annotation.Nonnull;

public class PagesSuggestionResponse extends ProfilesListResponse{

    @Nonnull
    private final List<BaseProfile> pages;

    public PagesSuggestionResponse(final int count, @Nullable final String next, @Nullable final String previous, @Nonnull final List<BaseProfile> pages) {
        super(count, next, previous, pages);
        this.pages = pages;
    }

    @Nonnull
    @Override
    public List<BaseProfile> getResults() {
        return pages;
    }
}
