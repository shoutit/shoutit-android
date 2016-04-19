package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreateShoutResponse {

    private final String id;
    private final String webUrl;
    private final String title;

    public CreateShoutResponse(@NonNull String id, String webUrl, String title) {
        this.id = id;
        this.webUrl = webUrl;
        this.title = title;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getWebUrl() {
        return webUrl;
    }

    @NonNull
    public String getTitle() {
        return title;
    }
}