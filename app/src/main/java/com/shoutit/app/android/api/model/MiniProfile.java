package com.shoutit.app.android.api.model;

public class MiniProfile {

    private final String id;
    private final String username;
    private final String name;

    public MiniProfile(String id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }
}
