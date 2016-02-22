package com.shoutit.app.android.api.model;


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
    private final boolean isPasswordSet;
    private final UserLocation location;

    public User(String id, String type, String apiUrl, String webUrl, String username,
                String name, String firstName, String lastName, boolean isActivated, String image,
                String cover, boolean isListening, boolean isPasswordSet, UserLocation location) {
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
        this.isPasswordSet = isPasswordSet;
        this.location = location;
    }

    // TODO remove it when user will be handler by API
    public static User guestUser(UserLocation location) {
        return new User(null, null, null, null ,null, null, null, null, false, null, null, false, false, location);
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
}
