package com.shoutit.app.android.api.model;

public class Tag {
    private final String id;
    private final String name;
    private final String apiUrl;
    private final String image;

    public Tag(String id, String name, String apiUrl, String image) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getImage() {
        return image;
    }
}
