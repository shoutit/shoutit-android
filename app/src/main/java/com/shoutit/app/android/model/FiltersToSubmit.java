package com.shoutit.app.android.model;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class FiltersToSubmit {
    @Nullable
    private final Integer minPrice;
    @Nullable
    private final Integer maxPrice;
    @Nonnull
    private final UserLocation userLocation;
    @Nullable
    private final Integer distance;
    @Nonnull
    private final String shoutType;
    @Nonnull
    private final SortType sortType;
    @Nullable
    private final Multimap<String, CategoryFilter.FilterValue> selectedFilters;

    public FiltersToSubmit(@Nullable String minPrice, @Nullable String maxPrice, @Nonnull UserLocation userLocation, Integer distance,
                           @Nonnull String shoutType, @Nonnull SortType sortType, @Nullable Multimap<String, CategoryFilter.FilterValue> selectedFilters) {
        this.sortType = sortType;
        this.minPrice = Ints.tryParse(Strings.nullToEmpty(minPrice));
        this.maxPrice = Ints.tryParse(Strings.nullToEmpty(maxPrice));
        this.userLocation = userLocation;
        this.distance = (distance == null || distance == 0) ? null : distance;
        this.shoutType = shoutType;
        this.selectedFilters = selectedFilters;
    }

    @Nullable
    public String getCity() {
        if (isEntireCountryDistance()) {
            return null;
        } else {
            return userLocation.getCity();
        }
    }

    @Nullable
    public String getState() {
        if (isEntireCountryDistance()) {
            return null;
        } else {
            return userLocation.getState();
        }
    }

    @Nullable
    public Integer getMinPrice() {
        return minPrice;
    }

    @Nullable
    public Integer getMaxPrice() {
        return maxPrice;
    }

    @Nonnull
    public UserLocation getUserLocation() {
        return userLocation;
    }

    @Nullable
    public Integer getDistance() {
        return distance;
    }

    @Nullable
    public String getShoutType() {
        return shoutType;
    }

    @Nonnull
    public SortType getSortType() {
        return sortType;
    }

    public boolean isEntireCountryDistance() {
        return distance == null;
    }

    @Nullable
    public Map<String, String> getFiltersQueryMap() {
        if (selectedFilters == null) {
            return null;
        }

        final Map<String, String> queryMap = new HashMap<>();
        for (String filterSlug : selectedFilters.keySet()) {
            final StringBuilder stringBuilder = new StringBuilder();
            final Collection<CategoryFilter.FilterValue> filterValues = selectedFilters.get(filterSlug);

            String separator = "";
            for (CategoryFilter.FilterValue value : filterValues) {
                stringBuilder.append(separator).append(value.getSlug());
                separator = ",";
            }

            queryMap.put(filterSlug, stringBuilder.toString());
        }

        return ImmutableMap.copyOf(queryMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiltersToSubmit)) return false;
        final FiltersToSubmit that = (FiltersToSubmit) o;
        return Objects.equal(minPrice, that.minPrice) &&
                Objects.equal(maxPrice, that.maxPrice) &&
                Objects.equal(userLocation, that.userLocation) &&
                Objects.equal(distance, that.distance) &&
                Objects.equal(shoutType, that.shoutType) &&
                Objects.equal(sortType, that.sortType) &&
                Objects.equal(selectedFilters, that.selectedFilters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(minPrice, maxPrice, userLocation, distance, shoutType, sortType, selectedFilters);
    }
}