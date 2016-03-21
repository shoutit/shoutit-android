package com.shoutit.app.android.model;


import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.view.search.SearchPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SearchShoutPointer {

    @Nonnull
    private final String query;
    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nonnull
    private final UserLocation location;
    @Nullable
    private final String contextItemId;

    public SearchShoutPointer(@Nonnull String query, @Nonnull SearchPresenter.SearchType searchType,
                              @Nonnull UserLocation location, @Nullable String contextItemId) {
        this.query = query;
        this.searchType = searchType;
        this.location = location;
        this.contextItemId = contextItemId;
    }

    @Nonnull
    public String getQuery() {
        return query;
    }

    @Nonnull
    public SearchPresenter.SearchType getSearchType() {
        return searchType;
    }

    @Nonnull
    public UserLocation getLocation() {
        return location;
    }

    @Nullable
    public String getContextItemId() {
        return contextItemId;
    }
}
