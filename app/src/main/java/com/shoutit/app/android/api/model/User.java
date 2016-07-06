package com.shoutit.app.android.api.model;


import android.support.annotation.NonNull;

import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class User extends BaseProfile {
    public static final String ME = "me";
    public static final String USER = "user";
    public static final String PAGE = "page";

    public enum Gender {
        FEMALE("female"),
        MALE("male"),
        OTHER("other"),
        NOT_SPECIFIED("");

        @Nullable
        private final String genderInApi;

        Gender(@Nullable String genderInApi) {
            this.genderInApi = genderInApi;
        }

        @Nullable
        public String getGenderInApi() {
            return genderInApi;
        }
    }

    private final String apiUrl;
    private final String webUrl;
    // whever the profile is listening to you
    private final boolean isListener;
    private final boolean isPasswordSet;
    private final String bio;
    private final int dateJoined;
    private final Listening listeningCount;
    private final String about;
    private final String mobile;
    private final String website;
    private final ConversationDetails conversation;
    @Nullable
    private final String gender;
    @Nullable
    private final String birthday; // Formatted like YYYY-MM-DD
    @Nullable
    private final LinkedAccounts linkedAccounts;


    @Nullable
    private final User admin; // Field only for Page user.

    public User(String id, String type, String apiUrl, String webUrl, String username,
                String name, String firstName, String lastName, boolean isActivated, String image,
                String cover, boolean isListening, boolean isListener, boolean isPasswordSet, @Nullable UserLocation location,
                int listenersCount, String bio, int dateJoined,
                Listening listeningCount, boolean isOwner, String about, String mobile, String website, String email, ConversationDetails conversation,
                @Nullable String gender, @Nullable String birthday, @Nullable Stats stats, @Nullable LinkedAccounts linkedAccounts, User admin) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, location, isOwner, stats, email);
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        this.isListener = isListener;
        this.isPasswordSet = isPasswordSet;
        this.bio = bio;
        this.dateJoined = dateJoined;
        this.listeningCount = listeningCount;
        this.admin = admin;
        this.isOwner = isOwner;
        this.about = about;
        this.mobile = mobile;
        this.website = website;
        this.conversation = conversation;
        this.gender = gender;
        this.birthday = birthday;
        this.linkedAccounts = linkedAccounts;
    }

    @NonNull
    @Override
    public User getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                newIsListening, isListener, isPasswordSet, location,
                newListenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, getStats(), linkedAccounts, admin);
    }

    @Nonnull
    @Override
    public User withUpdatedStats(@Nonnull Stats newStats) {
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                isListening, isListener, isPasswordSet, location,
                listenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, newStats, linkedAccounts, admin);
    }

    @Nonnull
    public User withUnlinkedFacebook() {
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                isListening, isListener, isPasswordSet, location,
                listenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, getStats(),linkedAccounts.unlinkedFacebook(), admin);
    }

    @Nonnull
    public User withUnlinkedGoogle() {
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                isListening, isListener, isPasswordSet, location,
                listenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, getStats(),linkedAccounts.unlinkedGoogle(), admin);
    }

    @Nonnull
    public User withUpdatedGoogleAccount(@Nonnull String token) {
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                isListening, isListener, isPasswordSet, location,
                listenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, getStats(),linkedAccounts.updatedGoogle(token), admin);
    }
    public boolean isUser(@Nonnull User user) {
        return (USER.equals(user.type));
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

    public String getMobile() {
        return mobile;
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

    public String getBio() {
        return bio;
    }

    public long getDateJoinedInMillis() {
        return dateJoined * 1000L;
    }

    public Listening getListeningCount() {
        return listeningCount;
    }

    public boolean isListener() {
        return isListener;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public String getAbout() {
        return about;
    }

    public String getWebsite() {
        return website;
    }

    public ConversationDetails getConversation() {
        return conversation;
    }

    @Nullable
    public LinkedAccounts getLinkedAccounts() {
        return linkedAccounts;
    }

    @Nullable
    public String getGender() {
        return gender;
    }

    @Nullable
    public String getBirthday() {
        return birthday;
    }

    @Nullable
    public User getAdmin() {
        return isUser() ? null : admin;
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
                isOwner == user.isOwner &&
                Objects.equal(id, user.id) &&
                Objects.equal(type, user.type) &&
                Objects.equal(apiUrl, user.apiUrl) &&
                Objects.equal(webUrl, user.webUrl) &&
                Objects.equal(website, user.website) &&
                Objects.equal(username, user.username) &&
                Objects.equal(name, user.name) &&
                Objects.equal(firstName, user.firstName) &&
                Objects.equal(lastName, user.lastName) &&
                Objects.equal(image, user.image) &&
                Objects.equal(cover, user.cover) &&
                Objects.equal(location, user.location) &&
                Objects.equal(bio, user.bio) &&
                Objects.equal(isListener, user.isListener) &&
                Objects.equal(birthday, user.birthday) &&
                Objects.equal(gender, user.gender) &&
                Objects.equal(linkedAccounts, user.linkedAccounts) &&
                Objects.equal(listeningCount, user.listeningCount) &&
                Objects.equal(admin, user.admin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, apiUrl, webUrl, username, name, firstName, lastName,
                isActivated, image, cover, isListening, isPasswordSet, location, listenersCount,
                bio, dateJoined, listeningCount, isListener, isOwner, website,
                linkedAccounts, birthday, gender, admin);
    }
}
