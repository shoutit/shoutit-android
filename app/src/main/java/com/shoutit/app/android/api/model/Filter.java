package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class Filter {

    @Nonnull
    private final String name;
    @Nonnull
    private final String slug;
    @Nonnull
    private final FilterValue value;

    public Filter(@Nonnull String name, @Nonnull String slug, @Nonnull FilterValue value) {
        this.name = name;
        this.slug = slug;
        this.value = value;
    }

    @Nonnull
    public String getSlug() {
        return slug;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public FilterValue getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filter)) return false;
        final Filter filter = (Filter) o;
        return Objects.equal(slug, filter.slug);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(slug);
    }
}
