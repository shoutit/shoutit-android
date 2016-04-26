package com.shoutit.app.android.model;


import com.google.common.base.Objects;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.view.search.SearchPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SearchShoutPointer {

    @Nullable
    private final String query;
    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nullable
    private final UserLocation location;
    @Nullable
    private final String contextItemId;
    @Nullable
    FiltersToSubmit filtersToSubmit;


    public SearchShoutPointer(@Nullable String query, @Nonnull SearchPresenter.SearchType searchType,
                              @Nonnull UserLocation location, @Nullable String contextItemId) {
        this.query = query;
        this.searchType = searchType;
        this.location = location;
        this.contextItemId = contextItemId;
    }

    public SearchShoutPointer(@Nullable String query,
                              @Nonnull SearchPresenter.SearchType searchType,
                              @Nullable String contextItemId,
                              @Nonnull FiltersToSubmit filtersToSubmit) {
        this.query = query;
        this.searchType = searchType;
        this.contextItemId = contextItemId;
        this.filtersToSubmit = filtersToSubmit;
        this.location = filtersToSubmit.getUserLocation();
    }

    @Nullable
    public String getQuery() {
        return query;
    }

    @Nonnull
    public SearchPresenter.SearchType getSearchType() {
        return searchType;
    }

    @Nullable
    public UserLocation getLocation() {
        return location;
    }

    @Nullable
    public String getContextItemId() {
        return contextItemId;
    }

    @Nullable
    public FiltersToSubmit getFiltersToSubmit() {
        return filtersToSubmit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchShoutPointer)) return false;
        final SearchShoutPointer that = (SearchShoutPointer) o;
        return Objects.equal(query, that.query) &&
                searchType == that.searchType &&
                Objects.equal(location, that.location) &&
                Objects.equal(contextItemId, that.contextItemId) &&
                Objects.equal(filtersToSubmit, that.filtersToSubmit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(query, searchType, location, contextItemId, filtersToSubmit);
    }
}
