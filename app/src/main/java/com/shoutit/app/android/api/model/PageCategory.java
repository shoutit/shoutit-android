package com.shoutit.app.android.api.model;

import java.util.List;

public class PageCategory {

    private final String id;
    private final String name;
    private final String slug;
    private final String image;
    private final List<PageCategory> children;

    public PageCategory(String id, String name, String slug, String image, List<PageCategory> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.image = image;
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getImage() {
        return image;
    }

    public List<PageCategory> getChildren() {
        return children;
    }
}
