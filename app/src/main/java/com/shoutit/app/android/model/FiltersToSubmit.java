package com.shoutit.app.android.model;

import android.support.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.shoutit.app.android.api.model.CategoryFilter;
import com.shoutit.app.android.api.model.UserLocation;

import javax.annotation.Nonnull;

public class FiltersToSubmit {
    @Nullable
    private final Integer minPrice;
    @Nullable
    private final Integer maxPrice;
    @Nullable
    private final UserLocation userLocation;
    @Nullable
    private final Integer distance;
    @Nonnull
    private final String shoutType;
    @Nullable
    private final Multimap<String, CategoryFilter.FilterValue> selectedFilters;

    public FiltersToSubmit(@Nullable String minPrice, @Nullable String maxPrice, @Nullable UserLocation userLocation, Integer distance,
                            @Nonnull String shoutType, @Nullable Multimap<String, CategoryFilter.FilterValue> selectedFilters) {
        this.minPrice = minPrice == null ? null : Integer.valueOf(minPrice);
        this.maxPrice = maxPrice == null ? null : Integer.valueOf(maxPrice);
        this.userLocation = userLocation;
        this.distance = (distance == null || distance == 0) ? null : distance;
        this.shoutType = shoutType;
        this.selectedFilters = selectedFilters;
    }

    @Nullable
    public Integer getMinPrice() {
        return minPrice;
    }

    @Nullable
    public Integer getMaxPrice() {
        return maxPrice;
    }

    @Nullable
    public UserLocation getUserLocation() {
        return userLocation;
    }

    @Nullable
    public Integer getDistance() {
        return distance;
    }

    @Nonnull
    public String getShoutType() {
        return shoutType;
    }

    @Nullable
    public Multimap<String, CategoryFilter.FilterValue> getSelectedFilters() {
        return selectedFilters;
    }
}