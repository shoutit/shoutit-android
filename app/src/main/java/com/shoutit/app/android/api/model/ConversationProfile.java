package com.shoutit.app.android.api.model;

public class ConversationProfile {

    private final String id;
    private final String name;
    private final String username;
    private final String type;
    private final String image;

    public ConversationProfile(String id, String name, String username, String type, String image) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.type = type;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public boolean isPage(){
        return type.equals(ProfileType.PAGE);
    }
}