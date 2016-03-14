package com.shoutit.app.android.api.model;

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
}
