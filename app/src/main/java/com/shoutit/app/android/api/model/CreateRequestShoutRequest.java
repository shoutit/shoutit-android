package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreateRequestShoutRequest {

    private final String type = "request";
    private final String title;
    private final UserLocationSimple location;

    public CreateRequestShoutRequest(@NonNull String title, @NonNull UserLocationSimple location) {
        this.title = title;
        this.location = location;
    }
}
