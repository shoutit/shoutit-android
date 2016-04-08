package com.shoutit.app.android.api.model;

import java.util.Objects;

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
    private final String imageUrl;
    private final String cover;
    private final String isListening;
    private final String listenersCount;
    private final String isOwner;

    public CallerProfile(String id, String type, String apiUrl, String webUrl,
                         String username, String name, String firstName,
                         String lastName, boolean isActivated, String imageUrl,
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
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
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
        if (o == null || getClass() != o.getClass()) return false;
        CallerProfile that = (CallerProfile) o;
        return Objects.equals(isActivated, that.isActivated) &&
                Objects.equals(id, that.id) &&
                Objects.equals(type, that.type) &&
                Objects.equals(apiUrl, that.apiUrl) &&
                Objects.equals(webUrl, that.webUrl) &&
                Objects.equals(username, that.username) &&
                Objects.equals(name, that.name) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(cover, that.cover) &&
                Objects.equals(isListening, that.isListening) &&
                Objects.equals(listenersCount, that.listenersCount) &&
                Objects.equals(isOwner, that.isOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, apiUrl, webUrl, username, name, firstName,
                lastName, isActivated, imageUrl, cover, isListening, listenersCount, isOwner);
    }
}
