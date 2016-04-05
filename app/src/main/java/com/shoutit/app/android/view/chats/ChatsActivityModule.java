package com.shoutit.app.android.view.chats;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatsActivityModule {

    private final String conversationId;

    public ChatsActivityModule(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    public String getConversationId() {
        return conversationId;
    }
}