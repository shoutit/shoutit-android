package com.shoutit.app.android.api.model;

import java.util.List;

import javax.annotation.Nonnull;

public class CategoryFilter {

    @Nonnull
    private final String name;
    @Nonnull
    private final String slug;
    @Nonnull
    private final List<FilterValue> values;

    public CategoryFilter(@Nonnull String name, @Nonnull String slug, @Nonnull List<FilterValue> values) {
        this.name = name;
        this.slug = slug;
        this.values = values;
    }

    public class FilterValue {
        @Nonnull
        private final String name;
        @Nonnull
        private final String slug;

        public FilterValue(@Nonnull String name, @Nonnull String slug) {
            this.name = name;
            this.slug = slug;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Nonnull
        public String getSlug() {
            return slug;
        }
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public List<FilterValue> getValues() {
        return values;
    }

    @Nonnull
    public String getSlug() {
        return slug;
    }
}
