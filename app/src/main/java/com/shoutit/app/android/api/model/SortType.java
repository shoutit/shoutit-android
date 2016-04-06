package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class SortType {
    @Nonnull
    private final String type;
    @Nonnull
    private final String name;

    public SortType(@Nonnull String type, @Nonnull String name) {
        this.type = type;
        this.name = name;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SortType)) return false;
        final SortType sortType = (SortType) o;
        return Objects.equal(type, sortType.type) &&
                Objects.equal(name, sortType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, name);
    }
}
