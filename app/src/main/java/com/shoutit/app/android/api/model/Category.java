package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import java.util.List;

public class Category {

    @NonNull
    private final String name;

    @NonNull
    private final String slug;

    @NonNull
    private final String icon;

    @NonNull
    private final String image;

    @NonNull
    private final List<CategoryFilter> filters;

    public Category(@NonNull String name,
                    @NonNull String slug,
                    @NonNull String icon,
                    @NonNull String image,
                    @NonNull List<CategoryFilter> filters) {
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.image = image;
        this.filters = filters;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getSlug() {
        return slug;
    }

    @NonNull
    public String getIcon() {
        return icon;
    }

    @NonNull
    public String getImage() {
        return image;
    }

    @NonNull
    public List<CategoryFilter> getFilters() {
        return filters;
    }
}
