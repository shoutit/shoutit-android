package com.shoutit.app.android.model;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import com.shoutit.app.android.api.model.FilterValue;
import com.shoutit.app.android.api.model.SortType;
import com.shoutit.app.android.api.model.UserLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class FiltersToSubmit {
    @Nullable
    private final Integer minPriceInCents;
    @Nullable
    private final Integer maxPriceInCents;
    @Nonnull
    private final UserLocation userLocation;
    @Nullable
    private final Integer distance;
    @Nonnull
    private final String shoutType;
    @Nonnull
    private final SortType sortType;
    @Nullable
    private final Multimap<String, FilterValue> selectedFilters;
    @Nullable
    private final String categorySlug;

    public FiltersToSubmit(@Nullable String minPriceInCents, @Nullable String maxPriceInCents, @Nonnull UserLocation userLocation, Integer distance,
                           @Nonnull String shoutType, @Nonnull SortType sortType, @Nullable Multimap<String, FilterValue> selectedFilters,
                           @Nullable String categorySlug) {
        this.sortType = sortType;
        this.categorySlug = categorySlug;
        this.minPriceInCents = priceToCents(Ints.tryParse(Strings.nullToEmpty(minPriceInCents)));
        this.maxPriceInCents = priceToCents(Ints.tryParse(Strings.nullToEmpty(maxPriceInCents)));
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
    private Integer priceToCents(@Nullable Integer price) {
        if (price != null) {
            return price * 100;
        } else {
            return null;
        }
    }

    @Nullable
    public Integer getMinPriceInCents() {
        return minPriceInCents;
    }

    @Nullable
    public Integer getMaxPriceInCents() {
        return maxPriceInCents;
    }

    @Nonnull
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

    @Nonnull
    public SortType getSortType() {
        return sortType;
    }

    @Nullable
    public String getCategorySlug() {
        return categorySlug;
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
            final Collection<FilterValue> filterValues = selectedFilters.get(filterSlug);

            String separator = "";
            for (FilterValue value : filterValues) {
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
        return Objects.equal(minPriceInCents, that.minPriceInCents) &&
                Objects.equal(maxPriceInCents, that.maxPriceInCents) &&
                Objects.equal(userLocation, that.userLocation) &&
                Objects.equal(distance, that.distance) &&
                Objects.equal(shoutType, that.shoutType) &&
                Objects.equal(sortType, that.sortType) &&
                Objects.equal(selectedFilters, that.selectedFilters) &&
                Objects.equal(categorySlug, that.categorySlug);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(minPriceInCents, maxPriceInCents, userLocation, distance, shoutType, sortType, selectedFilters, categorySlug);
    }
}