package com.shoutit.app.android.api.model;

public interface ProfileType {
    public static final String PAGE = "Page";
    public static final String PROFILE = "Profile";

    String getUserName();

    String getType();

    boolean isListening();
}
