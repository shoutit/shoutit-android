package com.shoutit.app.android.view.chats.chat_info.chats_blocked;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatBlockedUsersModule {

    private final String conversationId;

    public ChatBlockedUsersModule(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    public String getConversationId() {
        return conversationId;
    }
}
