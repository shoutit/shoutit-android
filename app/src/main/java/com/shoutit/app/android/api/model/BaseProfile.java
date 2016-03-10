package com.shoutit.app.android.api.model;


public abstract class BaseProfile implements ProfileType {
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

    protected BaseProfile(String id, String type, String username, String name,
                          String firstName, String lastName, boolean isActivated, String image,
                          String cover, boolean isListening, int listenersCount) {
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

    protected boolean isUser() {
        return USER.equals(type);
    }

}
