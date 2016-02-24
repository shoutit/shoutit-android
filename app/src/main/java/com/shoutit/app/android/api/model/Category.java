package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class Category {

    @NonNull
    private final String name;

    @NonNull
    private final String slug;

    @NonNull
    private final String icon;

    @NonNull
    private final Tag mainTag;

    public Category(@NonNull String name, @NonNull String slug, @NonNull String icon, @NonNull Tag mainTag) {
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.mainTag = mainTag;
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
}
