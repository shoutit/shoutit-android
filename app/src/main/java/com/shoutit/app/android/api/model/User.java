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

    public User(String id, String type, String apiUrl, String webUrl, String username,
                String name, String firstName, String lastName, boolean isActivated, String image,
                String cover, boolean isListening) {
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
    }
}
