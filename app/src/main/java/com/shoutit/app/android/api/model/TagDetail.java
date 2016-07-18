package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class TagDetail implements ProfileType {
    @Nonnull
    private final String id;
    @Nonnull
    private final String slug;
    private final String name;
    private final String apiUrl;
    private final String image;
    private final String cover;
    private final String webUrl;
    private final int listenersCount;
    private final boolean isListening;

    public TagDetail(@Nonnull String id, @Nonnull String slug, String name, String apiUrl, String image,
                     String cover, String webUrl, int listenersCount, boolean isListening) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.apiUrl = apiUrl;
        this.image = image;
        this.cover = cover;
        this.webUrl = webUrl;
        this.listenersCount = listenersCount;
        this.isListening = isListening;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Override
    @Nonnull
    public String getUsername() {
        // It is equivalent for user/page profile username
        return slug;
    }

    @Nonnull
    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String getType() {
        return ProfileType.TAG;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public int getListenersCount() {
        return listenersCount;
    }

    public boolean isListening() {
        return isListening;
    }

    public String getCover() {
        return cover;
    }

    public TagDetail toListenedTag() {
        final boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new TagDetail(id, slug, name, apiUrl, image, cover, webUrl, newListenersCount, newIsListening);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagDetail)) return false;
        final TagDetail tagDetail = (TagDetail) o;
        return listenersCount == tagDetail.listenersCount &&
                isListening == tagDetail.isListening &&
                Objects.equal(id, tagDetail.id) &&
                Objects.equal(slug, tagDetail.slug) &&
                Objects.equal(name, tagDetail.name) &&
                Objects.equal(apiUrl, tagDetail.apiUrl) &&
                Objects.equal(image, tagDetail.image) &&
                Objects.equal(cover, tagDetail.cover) &&
                Objects.equal(webUrl, tagDetail.webUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, slug, name, apiUrl, image, cover, webUrl, listenersCount, isListening);
    }
}
