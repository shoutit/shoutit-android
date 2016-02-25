package com.shoutit.app.android.api.model;

public class Page {
    private final String id;
    private final String type;
    private final String userName;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final boolean isActivated;
    private final String image;
    private final String cover;
    private final boolean isListening;
    private final int listenersCount;

    public Page(String id, String type, String userName, String name, String firstName,
                String lastName, boolean isActivated, String image, String cover, boolean isListening, int listenersCount) {
        this.id = id;
        this.type = type;
        this.userName = userName;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActivated = isActivated;
        this.image = image;
        this.cover = cover;
        this.isListening = isListening;
        this.listenersCount = listenersCount;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUserName() {
        return userName;
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

    public int getListenersCount() {
        return listenersCount;
    }
}
