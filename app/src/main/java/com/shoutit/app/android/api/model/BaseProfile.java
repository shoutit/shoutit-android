package com.shoutit.app.android.api.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;

public class BaseProfile implements ProfileType {

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
    private final Stats stats;
    @NonNull
    private final String email;
    @javax.annotation.Nullable
    protected final LinkedAccounts linkedAccounts;
    @Nullable
    protected final ConversationDetails conversation;
    // whenever the profile is listening to you
    protected final boolean isListener;

    public BaseProfile(@NonNull String id, @NonNull String type, @NonNull String username, @NonNull String name,
                       @NonNull String firstName, @NonNull String lastName, boolean isActivated, @Nullable String image,
                       @Nullable String cover, boolean isListening, int listenersCount, @Nullable UserLocation location,
                       boolean isOwner, @Nullable Stats stats, @NonNull String email, @Nullable LinkedAccounts linkedAccounts, ConversationDetails conversation, boolean isListener) {
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

    @Nonnull
    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, newIsListening, newListenersCount, location, isOwner, stats, email, linkedAccounts, conversation, isListener);
    }

    @Nonnull
    public BaseProfile withUpdatedStats(@Nonnull Stats newStats) {
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, isListening, listenersCount,
                location, isOwner, newStats, getEmail(), linkedAccounts, conversation, isListener);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseProfile)) return false;
        final BaseProfile profile = (BaseProfile) o;
        return isActivated == profile.isActivated &&
                isListening == profile.isListening &&
                isListener == profile.isListener &&
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
                Objects.equal(conversation, profile.conversation) &&
                Objects.equal(cover, profile.cover);
    }

    @Nullable
    public LinkedAccounts getLinkedAccounts() {
        return linkedAccounts;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, username, name, firstName, lastName,
                isActivated, image, cover, isListening, listenersCount, location, isOwner,
                conversation, isListener);
    }
}
