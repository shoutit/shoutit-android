package com.shoutit.app.android.view.profile.page;

import android.support.annotation.NonNull;

import javax.annotation.Nullable;

public class ChatInfo {
    @NonNull
    private final String username;
    @Nullable
    private final String conversationId;
    private final boolean isListening;
    private final boolean mIsUserLoggedIn;

    public ChatInfo(@NonNull String username, @Nullable String conversationId, boolean isListener, boolean isUserLoggedIn) {
        this.username = username;
        this.conversationId = conversationId;
        this.isListening = isListener;
        mIsUserLoggedIn = isUserLoggedIn;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getConversationId() {
        return conversationId;
    }

    public boolean isListener() {
        return isListening;
    }

    public boolean isUserLoggedIn() {
        return mIsUserLoggedIn;
    }
}