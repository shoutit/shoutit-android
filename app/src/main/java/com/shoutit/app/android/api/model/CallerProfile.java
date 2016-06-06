package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;

public class CallerProfile {

    private final String id;
    private final String type;
    private final String apiUrl;
    private final String webUrl;
    private final String username;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final boolean isActivated;
    private final String image;
    private final String cover;
    private final String isListening;
    private final String listenersCount;
    private final String isOwner;

    public CallerProfile(String id, String type, String apiUrl, String webUrl,
                         String username, String name, String firstName,
                         String lastName, boolean isActivated, String image,
                         String cover, String isListening, String listenersCount, String isOwner) {
        this.id = id;
        this.type = type;
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        this.username = username;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActivated = isActivated;
        this.image = image;
        this.cover = cover;
        this.isListening = isListening;
        this.listenersCount = listenersCount;
        this.isOwner = isOwner;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public String getImage() {
        return image;
    }

    public String getCover() {
        return cover;
    }

    public String getIsListening() {
        return isListening;
    }

    public String getListenersCount() {
        return listenersCount;
    }

    public String getIsOwner() {
        return isOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallerProfile)) return false;
        final CallerProfile that = (CallerProfile) o;
        return isActivated == that.isActivated &&
                Objects.equal(id, that.id) &&
                Objects.equal(type, that.type) &&
                Objects.equal(apiUrl, that.apiUrl) &&
                Objects.equal(webUrl, that.webUrl) &&
                Objects.equal(username, that.username) &&
                Objects.equal(name, that.name) &&
                Objects.equal(firstName, that.firstName) &&
                Objects.equal(lastName, that.lastName) &&
                Objects.equal(image, that.image) &&
                Objects.equal(cover, that.cover) &&
                Objects.equal(isListening, that.isListening) &&
                Objects.equal(listenersCount, that.listenersCount) &&
                Objects.equal(isOwner, that.isOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover, isListening, listenersCount, isOwner);
    }
}
