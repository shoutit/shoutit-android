package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;

import java.util.List;

import javax.annotation.Nonnull;

public class User {
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
    private final boolean isListening;
    private final boolean isListener;
    private final boolean isPasswordSet;
    private final UserLocation location;
    private final int listenersCount;
    private final List<Page> pages;
    private final String bio;
    private final int dateJoined;
    private final Listening listeningCount;

    public User(String id, String type, String apiUrl, String webUrl, String username,
                String name, String firstName, String lastName, boolean isActivated, String image,
                String cover, boolean isListening, boolean isListener, boolean isPasswordSet, UserLocation location, int listenersCount, List<Page> pages, String bio, int dateJoined, Listening listeningCount) {
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
        this.isListener = isListener;
        this.isPasswordSet = isPasswordSet;
        this.location = location;
        this.listenersCount = listenersCount;
        this.pages = pages;
        this.bio = bio;
        this.dateJoined = dateJoined;
        this.listeningCount = listeningCount;
    }

    // TODO remove it when user will be handler by API
    public static User guestUser(UserLocation location) {
        return new User(null, null, null, null ,null, null, null, null, false, null, null, false, false, false, location, 1, null, null, 0, null);
    }

    public static User listenedUser(@Nonnull User user, boolean isListening) {
        int listenersCount = isListening ? user.listenersCount + 1 : user.listenersCount - 1;
        return new User(user.id, user.type, user.apiUrl, user.webUrl, user.username, user.name,
                user.firstName, user.lastName, user.isActivated, user.image, user.cover,
                isListening, user.isListener, user.isPasswordSet, user.location,
                listenersCount, user.pages, user.bio, user.dateJoined, user.listeningCount);
    }

    public static User userWithUpdatedPages(@Nonnull User user, List<Page> pages) {
        return new User(user.id, user.type, user.apiUrl, user.webUrl, user.username, user.name,
                user.firstName, user.lastName, user.isActivated, user.image, user.cover,
                user.isListening, user.isListener, user.isPasswordSet, user.location,
                user.listenersCount, pages, user.bio, user.dateJoined, user.listeningCount);
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

    public boolean isListening() {
        return isListening;
    }

    public UserLocation getLocation() {
        return location;
    }

    public boolean isPasswordSet() {
        return isPasswordSet;
    }

    public int getListenersCount() {
        return listenersCount;
    }

    public List<Page> getPages() {
        return pages;
    }

    public String getBio() {
        return bio;
    }

    public long getDateJoinedInMillis() {
        return dateJoined * 1000;
    }

    public Listening getListeningCount() {
        return listeningCount;
    }

    public boolean isListener() {
        return isListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        final User user = (User) o;
        return isActivated == user.isActivated &&
                isListening == user.isListening &&
                isPasswordSet == user.isPasswordSet &&
                listenersCount == user.listenersCount &&
                dateJoined == user.dateJoined &&
                Objects.equal(id, user.id) &&
                Objects.equal(type, user.type) &&
                Objects.equal(apiUrl, user.apiUrl) &&
                Objects.equal(webUrl, user.webUrl) &&
                Objects.equal(username, user.username) &&
                Objects.equal(name, user.name) &&
                Objects.equal(firstName, user.firstName) &&
                Objects.equal(lastName, user.lastName) &&
                Objects.equal(image, user.image) &&
                Objects.equal(cover, user.cover) &&
                Objects.equal(location, user.location) &&
                Objects.equal(pages, user.pages) &&
                Objects.equal(bio, user.bio) &&
                Objects.equal(isListener, user.isListener) &&
                Objects.equal(listeningCount, user.listeningCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, apiUrl, webUrl, username, name, firstName, lastName,
                isActivated, image, cover, isListening, isPasswordSet, location, listenersCount,
                pages, bio, dateJoined, listeningCount, isListener);
    }
}
