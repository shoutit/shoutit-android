package com.shoutit.app.android.model;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class LocationPointer {

    @Nullable
    private final String countryCode;
    @Nonnull
    private final String city;

    public LocationPointer(@Nullable String countryCode, @Nonnull String city) {
        this.countryCode = countryCode;
        this.city = city;
    }

    @Nullable
    public String getCountryCode() {
        return countryCode;
    }

    @Nonnull
    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationPointer)) return false;
        final LocationPointer that = (LocationPointer) o;
        return Objects.equal(countryCode, that.countryCode) &&
                Objects.equal(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(countryCode, city);
    }
}
