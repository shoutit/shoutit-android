package com.shoutit.app.android.api.model;

import com.google.common.base.Objects;

public class Page implements ProfileType {
    private final String id;
    private final String type;
    private final String username;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final boolean isActivated;
    private final String image;
    private final String cover;
    private final boolean isListening;
    private final int listenersCount;

    public Page(String id, String type, String username, String name, String firstName,
                String lastName, boolean isActivated, String image, String cover, boolean isListening, int listenersCount) {
        this.id = id;
        this.type = type;
        this.username = username;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActivated = isActivated;
        this.image = image;
        this.cover = cover;
        this.isListening = isListening;
        this.listenersCount = listenersCount;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
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

    @Override
    public boolean isListening() {
        return isListening;
    }

    public int getListenersCount() {
        return listenersCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        final Page page = (Page) o;
        return isActivated == page.isActivated &&
                isListening == page.isListening &&
                listenersCount == page.listenersCount &&
                Objects.equal(id, page.id) &&
                Objects.equal(type, page.type) &&
                Objects.equal(username, page.username) &&
                Objects.equal(name, page.name) &&
                Objects.equal(firstName, page.firstName) &&
                Objects.equal(lastName, page.lastName) &&
                Objects.equal(image, page.image) &&
                Objects.equal(cover, page.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount);
    }

    public static Page withIsListening(Page page, boolean isListening) {
        int listenersCount = isListening ? page.listenersCount + 1 : page.listenersCount - 1;
        return new Page(page.id, page.type, page.username, page.name, page.firstName,
                page.lastName, page.isActivated, page.image, page.cover, isListening, listenersCount);
    }
}
