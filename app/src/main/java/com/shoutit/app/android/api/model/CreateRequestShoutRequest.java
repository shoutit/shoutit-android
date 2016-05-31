package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreateRequestShoutRequest {

    private final String type = "request";
    private final String title;
    private final UserLocationSimple location;
    private final boolean publishToFacebook;

    public CreateRequestShoutRequest(@NonNull String title, @NonNull UserLocationSimple location,
                                     boolean publishToFacebook) {
        this.title = title;
        this.location = location;
        this.publishToFacebook = publishToFacebook;
    }
}
