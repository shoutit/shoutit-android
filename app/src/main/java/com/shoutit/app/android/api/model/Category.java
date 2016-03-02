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
    private final String image;


    public Category(@NonNull String name, @NonNull String slug, @NonNull String icon, @NonNull String image) {
        this.name = name;
        this.slug = slug;
        this.icon = icon;
        this.image = image;
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
}
