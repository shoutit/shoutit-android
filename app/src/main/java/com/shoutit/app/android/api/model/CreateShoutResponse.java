package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreateShoutResponse {

    private final String id;
    private final String webUrl;

    public CreateShoutResponse(@NonNull String id, String webUrl) {
        this.id = id;
        this.webUrl = webUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getWebUrl() {
        return webUrl;
    }
}
