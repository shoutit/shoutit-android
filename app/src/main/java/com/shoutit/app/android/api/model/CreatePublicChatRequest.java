package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class CreatePublicChatRequest {

    private final String subject;

    public CreatePublicChatRequest(@NonNull String subject) {
        this.subject = subject;
    }
}
