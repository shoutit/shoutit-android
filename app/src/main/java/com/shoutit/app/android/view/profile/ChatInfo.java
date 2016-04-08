package com.shoutit.app.android.view.profile;

import android.support.annotation.NonNull;

import javax.annotation.Nullable;

public class ChatInfo {
    @NonNull
    private final String username;
    @Nullable
    private final String conversationId;
    private final boolean isListening;

    public ChatInfo(@NonNull String username, @Nullable String conversationId, boolean isListening) {
        this.username = username;
        this.conversationId = conversationId;
        this.isListening = isListening;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getConversationId() {
        return conversationId;
    }

    public boolean isListening() {
        return isListening;
    }
}