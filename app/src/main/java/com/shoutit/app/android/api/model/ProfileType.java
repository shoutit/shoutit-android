package com.shoutit.app.android.api.model;

public interface ProfileType {
    public static final String PAGE = "Page";
    public static final String PROFILE = "Profile";

    String getUsername();

    String getName();

    String getImage();

    String getType();

    boolean isListening();

    int getListenersCount();
}
