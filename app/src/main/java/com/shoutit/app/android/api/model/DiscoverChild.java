package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscoverChild)) return false;
        final DiscoverChild that = (DiscoverChild) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(apiUrl, that.apiUrl) &&
                Objects.equal(title, that.title) &&
                Objects.equal(subtitle, that.subtitle) &&
                Objects.equal(image, that.image) &&
                Objects.equal(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, apiUrl, title, subtitle, image, icon);
    }
}