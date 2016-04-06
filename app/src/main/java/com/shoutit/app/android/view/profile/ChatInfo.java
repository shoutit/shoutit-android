package com.shoutit.app.android.view.profile;

import android.support.annotation.NonNull;

import javax.annotation.Nullable;

public class ChatInfo {
    @NonNull
    private final String username;
    @Nullable
    private final String conversationId;

    public ChatInfo(@NonNull String username, @Nullable String conversationId) {
        this.username = username;
        this.conversationId = conversationId;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getConversationId() {
        return conversationId;
    }
}