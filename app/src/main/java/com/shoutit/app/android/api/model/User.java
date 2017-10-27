package com.shoutit.app.android.api.model;


import android.support.annotation.NonNull;

import com.google.common.base.Objects;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class User extends BaseProfile {
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
    private final boolean isPasswordSet;
    private final String bio;
    private final int dateJoined;
    private final Listening listeningCount;
    private final String about;
    private final String mobile;
    @Nullable
    private final String gender;
    @Nullable
    private final String birthday; // Formatted like YYYY-MM-DD

    @Nullable
    private final User admin; // Field only for Page user.
    private final boolean isVerified; // Field only for Page user.

    public User(String id, String type, String apiUrl, String webUrl, String username,
                String name, String firstName, String lastName, boolean isActivated, String image,
                String cover, boolean isListening, boolean isListener, boolean isPasswordSet, @Nullable UserLocation location,
                int listenersCount, String bio, int dateJoined,
                Listening listeningCount, boolean isOwner, String about, String mobile,
                String website, String email, ConversationDetails conversation,
                @Nullable String gender, @Nullable String birthday, @Nullable Stats stats,
                @Nullable LinkedAccounts linkedAccounts, @Nullable User admin, boolean isVerified) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount, location, isOwner, stats, email, linkedAccounts, conversation, isListener, null);
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        this.isPasswordSet = isPasswordSet;
        this.bio = bio;
        this.dateJoined = dateJoined;
        this.listeningCount = listeningCount;
        this.admin = admin;
        this.isVerified = isVerified;
        this.isOwner = isOwner;
        this.about = about;
        this.mobile = mobile;
        this.gender = gender;
        this.birthday = birthday;
    }

    @NonNull
    @Override
    public User getListenedProfile(int newListenersCount) {
        boolean newIsListening = !isListening;

        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                newIsListening, isListener, isPasswordSet, location,
                newListenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, getStats(), linkedAccounts, admin, isVerified);
    }

    @Nonnull
    @Override
    public User withUpdatedStats(@Nonnull Stats newStats) {
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                isListening, isListener, isPasswordSet, location,
                listenersCount, bio, dateJoined, listeningCount,
                false, about, mobile, website, getEmail(), conversation, gender, birthday, newStats, linkedAccounts, admin, isVerified);
    }

    public boolean isUser(@Nonnull User user) {
        return (USER.equals(user.type));
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
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

    public boolean isVerified() {
        return isVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        if (!super.equals(o)) return false;
        final User user = (User) o;
        return isPasswordSet == user.isPasswordSet &&
                dateJoined == user.dateJoined &&
                isVerified == user.isVerified &&
                Objects.equal(apiUrl, user.apiUrl) &&
                Objects.equal(webUrl, user.webUrl) &&
                Objects.equal(bio, user.bio) &&
                Objects.equal(listeningCount, user.listeningCount) &&
                Objects.equal(about, user.about) &&
                Objects.equal(mobile, user.mobile) &&
                Objects.equal(gender, user.gender) &&
                Objects.equal(birthday, user.birthday) &&
                Objects.equal(admin, user.admin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), apiUrl, webUrl, isPasswordSet, bio,
                dateJoined, listeningCount, about, mobile, gender, birthday, admin, isVerified);
    }
}
