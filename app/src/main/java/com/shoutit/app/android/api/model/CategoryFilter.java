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
        return Objects.equal(slug, filter.slug);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(slug);
    }

    @Nullable
    public FilterValue getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(@Nullable FilterValue selectedValue) {
        this.selectedValue = selectedValue;
    }
}
