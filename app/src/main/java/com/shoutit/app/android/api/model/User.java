package com.shoutit.app.android.api.model;


import com.google.common.base.Objects;

import java.util.List;

import javax.annotation.Nonnull;

public class User extends BaseProfile {
    public static final String ME = "me";

    private final String apiUrl;
    private final String webUrl;
    private final boolean isListener;
    private final boolean isPasswordSet;
    private final UserLocation location;
    private final List<Page> pages;
    private final List<Admin> admins;
    private final String bio;
    private final int dateJoined;
    private final Listening listeningCount;
    private final boolean isOwner;
    private final String about;
    private final String mobile;
    private final String website;
    private final Conversation conversation;

    public User(String id, String type, String apiUrl, String webUrl, String username,
                String name, String firstName, String lastName, boolean isActivated, String image,
                String cover, boolean isListening, boolean isListener, boolean isPasswordSet, UserLocation location,
                int listenersCount, List<Page> pages, List<Admin> admins, String bio, int dateJoined,
                Listening listeningCount, boolean isOwner, String about, String mobile, String website, Conversation conversation) {
        super(id, type, username, name, firstName, lastName, isActivated, image, cover, isListening, listenersCount);
        this.apiUrl = apiUrl;
        this.webUrl = webUrl;
        this.isListener = isListener;
        this.isPasswordSet = isPasswordSet;
        this.location = location;
        this.pages = pages;
        this.admins = admins;
        this.bio = bio;
        this.dateJoined = dateJoined;
        this.listeningCount = listeningCount;
        this.isOwner = isOwner;
        this.about = about;
        this.mobile = mobile;
        this.website = website;
        this.conversation = conversation;
    }

    // TODO remove it when user will be handler by API
    public static User guestUser(UserLocation location) {
        return new User(null, null, null, null ,null, null, null, null, false, null, null, false, false, false, location, 1, null, null, null, 0, null, false, null, null, null, null);
    }

    public User getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new User(id, type, apiUrl, webUrl, username, name,
                firstName, lastName, isActivated, image, cover,
                newIsListening, isListener, isPasswordSet, location,
                newListenersCount, pages, admins, bio, dateJoined, listeningCount,
                false, about, mobile, website, conversation);
    }

    public static User userWithUpdatedPages(@Nonnull User user, List<Page> pages) {
        return new User(user.id, user.type, user.apiUrl, user.webUrl, user.username, user.name,
                user.firstName, user.lastName, user.isActivated, user.image, user.cover,
                user.isListening, user.isListener, user.isPasswordSet, user.location,
                user.listenersCount, pages, user.admins, user.bio, user.dateJoined, user.listeningCount,
                false, user.about, user.mobile, user.website, user.conversation);
    }

    public static User userWithUpdatedAdmins(@Nonnull User user, List<Admin> updatedAdmins) {
        return new User(user.id, user.type, user.apiUrl, user.webUrl, user.username, user.name,
                user.firstName, user.lastName, user.isActivated, user.image, user.cover,
                user.isListening, user.isListener, user.isPasswordSet, user.location,
                user.listenersCount, user.pages, updatedAdmins, user.bio, user.dateJoined, user.listeningCount,
                false, user.about, user.mobile, user.website, user.conversation);
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

    public List<Page> getPages() {
        return pages;
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

    public List<Admin> getAdmins() {
        return admins;
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

    public Conversation getConversation() {
        return conversation;
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
                Objects.equal(pages, user.pages) &&
                Objects.equal(admins, user.admins) &&
                Objects.equal(bio, user.bio) &&
                Objects.equal(isListener, user.isListener) &&
                Objects.equal(listeningCount, user.listeningCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, apiUrl, webUrl, username, name, firstName, lastName,
                isActivated, image, cover, isListening, isPasswordSet, location, listenersCount,
                pages, bio, dateJoined, listeningCount, isListener, admins, isOwner, website);
    }
}
