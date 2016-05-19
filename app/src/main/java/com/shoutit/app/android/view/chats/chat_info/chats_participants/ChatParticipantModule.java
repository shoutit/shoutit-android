package com.shoutit.app.android.view.chats.chat_info.chats_participants;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatParticipantModule {

    private final String conversationId;

    public ChatParticipantModule(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    public String getConversationId() {
        return conversationId;
    }
}
