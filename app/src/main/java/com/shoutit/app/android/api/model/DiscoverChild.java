package com.shoutit.app.android.api.model;

public class DiscoverChild {
    private final String id;
    private final String apiUrl;
    private final String title;
    private final String subtitle;
    private final String image;
    private final String icon;

    public DiscoverChild(String id, String apiUrl, String title, String subtitle, String image, String icon) {
        this.id = id;
        this.apiUrl = apiUrl;
        this.title = title;
        this.subtitle = subtitle;
        this.image = image;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImage() {
        return image;
    }

    public String getIcon() {
        return icon;
    }
}