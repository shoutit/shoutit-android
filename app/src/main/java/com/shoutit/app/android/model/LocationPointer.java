package com.shoutit.app.android.model;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class LocationPointer {

    @Nullable
    private final String countryCode;
    @Nonnull
    private final String city;
    @Nullable
    private final String state;

    public LocationPointer(@Nullable String countryCode, @Nonnull String city, String state) {
        this.countryCode = countryCode;
        this.city = city;
        this.state = state;
    }

    @Nullable
    public String getCountryCode() {
        return countryCode;
    }

    @Nonnull
    public String getCity() {
        return city;
    }

    @Nullable
    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationPointer)) return false;
        final LocationPointer that = (LocationPointer) o;
        return Objects.equal(countryCode, that.countryCode) &&
                Objects.equal(state, that.state) &&
                Objects.equal(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(countryCode, city, state);
    }
}
