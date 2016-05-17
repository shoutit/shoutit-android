package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class EditPublicChatRequest {

    private final String subject;

    public EditPublicChatRequest(@NonNull String subject) {
        this.subject = subject;
    }
}
