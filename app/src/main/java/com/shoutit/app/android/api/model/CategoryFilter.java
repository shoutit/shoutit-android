package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Nonnull;

public class CategoryFilter {

    @Nonnull
    private final String name;
    @Nonnull
    private final String slug;
    @Nonnull
    private final List<FilterValue> values;
    @Nullable
    @SerializedName("value")
    private FilterValue selectedValue;

    public CategoryFilter(@Nonnull String name, @Nonnull String slug,
                          @Nonnull List<FilterValue> values) {
        this.name = name;
        this.slug = slug;
        this.values = values;
    }

    public static class FilterValue {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterValue)) return false;
            final FilterValue that = (FilterValue) o;
            return Objects.equal(name, that.name) &&
                    Objects.equal(slug, that.slug);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, slug);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryFilter)) return false;
        final CategoryFilter filter = (CategoryFilter) o;
        return Objects.equal(name, filter.name) &&
                Objects.equal(slug, filter.slug) &&
                Objects.equal(values, filter.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, slug, values);
    }

    @Nullable
    public FilterValue getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(@Nullable FilterValue selectedValue) {
        this.selectedValue = selectedValue;
    }
}
