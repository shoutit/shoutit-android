package com.shoutit.app.android.view.chats.chat_info;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatInfoModule {

    private final String conversationId;

    public ChatInfoModule(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    public String getConversationId() {
        return conversationId;
    }
}
