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
    private final Tag mainTag;

    @NonNull
    private final List<CategoryFilter> filters;

    public Category(@NonNull String name,
                    @NonNull String slug,
                    @NonNull String icon,
                    @NonNull Tag mainTag,
                    @NonNull List<CategoryFilter> filters) {
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.mainTag = mainTag;
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
    public Tag getMainTag() {
        return mainTag;
    }

    @NonNull
    public String getIcon() {
        return icon;
    }

    @NonNull
    public List<CategoryFilter> getFilters() {
        return filters;
    }
}
