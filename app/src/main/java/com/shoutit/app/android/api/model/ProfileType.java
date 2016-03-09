package com.shoutit.app.android.api.model;

public interface ProfileType {
    String PAGE = "page";
    String USER = "user";

    String getUsername();

    String getName();

    String getImage();

    String getType();

    boolean isListening();

    int getListenersCount();
}
