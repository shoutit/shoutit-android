package com.shoutit.app.android.api.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;

public class BaseProfile implements ProfileType {
    public static final String ME = "me";

    @NonNull
    protected final String id;
    @NonNull
    protected final String type;
    @NonNull
    protected final String username;
    @NonNull
    protected final String name;
    @NonNull
    protected final String firstName;
    @NonNull
    protected final String lastName;
    protected final boolean isActivated;
    @Nullable
    protected final String image;
    @Nullable
    protected final String cover;
    protected final boolean isListening;
    protected final int listenersCount;
    @Nullable
    protected final UserLocation location;
    protected boolean isOwner;
    @Nullable
    protected final Stats stats;
    @NonNull
    protected final String email;
    @javax.annotation.Nullable
    protected final LinkedAccounts linkedAccounts;
    @Nullable
    protected final ConversationDetails conversation;
    // whenever the profile is listening to you
    protected final boolean isListener;
    @Nullable
    protected final String website;

    public BaseProfile(@NonNull String id, @NonNull String type, @NonNull String username, @NonNull String name,
                       @NonNull String firstName, @NonNull String lastName, boolean isActivated, @Nullable String image,
                       @Nullable String cover, boolean isListening, int listenersCount, @Nullable UserLocation location,
                       boolean isOwner, @Nullable Stats stats, @NonNull String email, @Nullable LinkedAccounts linkedAccounts,
                       @Nullable ConversationDetails conversation, boolean isListener, @Nullable String website) {
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
        this.linkedAccounts = linkedAccounts;
        this.conversation = conversation;
        this.isListener = isListener;
        this.website = website;
    }

    public boolean isUser() {
        return USER.equals(type);
    }

    public boolean isPage() {
        return PAGE.equals(type);
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String getType() {
        return type;
    }

    @NonNull
    @Override
    public String getUsername() {
        return username;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    public String getFirstName() {
        return firstName;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    public boolean isActivated() {
        return isActivated;
    }

    @Nullable
    @Override
    public String getImage() {
        return image;
    }

    @Nullable
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

    public boolean isListener() {
        return isListener;
    }

    @Nullable
    public String getWebsite() {
        return website;
    }

    @Nonnull
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, newIsListening, newListenersCount, location, isOwner, stats,
                email, linkedAccounts, conversation, isListener, website);
    }

    @Nonnull
    public BaseProfile withUpdatedStats(@Nonnull Stats newStats) {
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount,
                location, isOwner, newStats, getEmail(), linkedAccounts, conversation, isListener, website);
    }

    @Nonnull
    public BaseProfile withUnlinkedFacebook() {
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount,
                location, isOwner, stats, getEmail(), linkedAccounts.unlinkedFacebook(), conversation, isListener, website);
    }

    @Nonnull
    public BaseProfile withUnlinkedGoogle() {
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount,
                location, isOwner, stats, getEmail(), linkedAccounts.unlinkedGoogle(), conversation, isListener, website);
    }

    @Nonnull
    public BaseProfile withUpdatedGoogleAccount(@Nonnull String token) {
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount,
                location, isOwner, stats, getEmail(), linkedAccounts.updatedGoogle(token), conversation, isListener, website);
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

    @NonNull
    public String getEmail() {
        return email;
    }

    @Nullable
    public UserLocation getLocation() {
        return location;
    }

    @Nullable
    public ConversationDetails getConversation() {
        return conversation;
    }

    @Nullable
    public LinkedAccounts getLinkedAccounts() {
        return linkedAccounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseProfile)) return false;
        final BaseProfile that = (BaseProfile) o;
        return isActivated == that.isActivated &&
                isListening == that.isListening &&
                listenersCount == that.listenersCount &&
                isOwner == that.isOwner &&
                isListener == that.isListener &&
                Objects.equal(id, that.id) &&
                Objects.equal(type, that.type) &&
                Objects.equal(username, that.username) &&
                Objects.equal(name, that.name) &&
                Objects.equal(firstName, that.firstName) &&
                Objects.equal(lastName, that.lastName) &&
                Objects.equal(image, that.image) &&
                Objects.equal(cover, that.cover) &&
                Objects.equal(location, that.location) &&
                Objects.equal(stats, that.stats) &&
                Objects.equal(email, that.email) &&
                Objects.equal(linkedAccounts, that.linkedAccounts) &&
                Objects.equal(conversation, that.conversation) &&
                Objects.equal(website, that.website);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName,
                isActivated, image, cover, isListening, listenersCount, location,
                isOwner, stats, email, linkedAccounts, conversation, isListener, website);
    }
}
