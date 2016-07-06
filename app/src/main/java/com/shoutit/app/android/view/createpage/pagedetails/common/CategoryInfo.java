package com.shoutit.app.android.view.createpage.pagedetails.common;

public class CategoryInfo {
    private final String image;
    private final String name;
    private final String slug;

    public CategoryInfo(String image, String name, String id) {
        this.image = image;
        this.name = name;
        this.slug = id;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }
}