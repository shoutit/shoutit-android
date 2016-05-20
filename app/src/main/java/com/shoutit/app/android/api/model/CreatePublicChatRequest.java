package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreatePublicChatRequest {

    private final String subject;
    private final String icon;
    private final UserLocationSimple location;

    public CreatePublicChatRequest(@NonNull String subject, String icon, UserLocationSimple location) {
        this.subject = subject;
        this.icon = icon;
        this.location = location;
    }
}
