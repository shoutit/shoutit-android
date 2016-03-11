package com.shoutit.app.android.api.model;


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

    public BaseProfile(String id, String type, String username, String name,
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

    public BaseProfile getListenedProfile() {
        boolean newIsListening = !isListening;
        int newListenersCount = newIsListening ? listenersCount + 1 : listenersCount - 1;
        return new BaseProfile(id, type, username, name, firstName, lastName, isActivated,
                image, cover, newIsListening, newListenersCount);
    }
}
