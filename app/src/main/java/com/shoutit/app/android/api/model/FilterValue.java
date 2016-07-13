package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class FilterValue {
    @Nonnull
    private final String id;
    @Nonnull
    private final String name;
    @Nonnull
    private final String slug;

    public FilterValue(@Nonnull String id, @Nonnull String name, @Nonnull String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getSlug() {
        return slug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterValue)) return false;
        final FilterValue that = (FilterValue) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(name, that.name) &&
                Objects.equal(slug, that.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, slug);
    }
}