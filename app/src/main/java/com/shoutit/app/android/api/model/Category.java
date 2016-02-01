package com.shoutit.app.android.api.model;

public class Category {
    private final String name;
    private final String slug;
    private final Tag mainTag;

    public Category(String name, String slug, Tag mainTag) {
        this.name = name;
        this.slug = slug;
        this.mainTag = mainTag;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public Tag getMainTag() {
        return mainTag;
    }
}
