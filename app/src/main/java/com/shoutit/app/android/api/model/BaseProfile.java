package com.shoutit.app.android.api.model;


import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;

public class BaseProfile implements ProfileType {
    protected final String id;
    protected final String type;
    protected final String username;
    protected final String name;
    protected final String firstName;
    protected final String lastName;
    protected final boolean isActivated;
    protected final String image;
    protected final String cover;
    protected final boolean isListening;
    protected final int listenersCount;
    @Nullable
    protected final UserLocation location;
    protected boolean isOwner;
    @Nullable
    private final Stats stats;
    private final String email;

    public BaseProfile(String id, String type, String username, String name,
                       String firstName, String lastName, boolean isActivated, String image,
                       String cover, boolean isListening, int listenersCount, @Nullable UserLocation location,
                       boolean isOwner, @Nullable Stats stats, String email) {
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
        this.location = location;
        this.isOwner = isOwner;
        this.stats = stats;
        this.email = email;
    }

    public boolean isUser() {
        return USER.equals(type);
    }

    public boolean isPage() {
        return PAGE.equals(type);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
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

    @Override
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

    @Override
    public int getListenersCount() {
        return listenersCount;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, newIsListening, newListenersCount, location, isOwner, stats, email);
    }

    @Nonnull
    public BaseProfile withUpdatedStats(@Nonnull Stats newStats) {
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount,
                location, isOwner, newStats, getEmail());
    }

    @Nullable
    public Stats getStats() {
        return stats;
    }

    public int getUnreadConversationsCount() {
        if (stats == null) {
            return 0;
        } else {
            return stats.getUnreadConversationsCount();
        }
    }

    public int getUnreadNotificationsCount() {
        if (stats == null) {
            return 0;
        } else {
            return stats.getUnreadNotifications();
        }
    }

    public String getEmail() {
        return email;
    }

    @Nullable
    public UserLocation getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseProfile)) return false;
        final BaseProfile profile = (BaseProfile) o;
        return isActivated == profile.isActivated &&
                isListening == profile.isListening &&
                listenersCount == profile.listenersCount &&
                Objects.equal(id, profile.id) &&
                Objects.equal(type, profile.type) &&
                Objects.equal(username, profile.username) &&
                Objects.equal(name, profile.name) &&
                Objects.equal(firstName, profile.firstName) &&
                Objects.equal(lastName, profile.lastName) &&
                Objects.equal(image, profile.image) &&
                Objects.equal(location, profile.location) &&
                Objects.equal(isOwner, profile.isOwner) &&
                Objects.equal(cover, profile.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName,
                isActivated, image, cover, isListening, listenersCount, location, isOwner);
    }
}
