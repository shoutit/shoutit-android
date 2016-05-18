package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

public class EditPublicChatRequest {

    private final String subject;
    private final String icon;

    public EditPublicChatRequest(@NonNull String subject, String icon) {
        this.subject = subject;
        this.icon = icon;
    }
}
