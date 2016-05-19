package com.shoutit.app.android.view.chats.chat_info.chats_users_list;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatUsersListModule {

    private final String conversationId;

    public ChatUsersListModule(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    public String getConversationId() {
        return conversationId;
    }
}
