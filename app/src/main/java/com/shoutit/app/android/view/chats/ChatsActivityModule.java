package com.shoutit.app.android.view.chats;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatsActivityModule {

    private final String conversationId;
    private final boolean mIsShoutConversation;

    public ChatsActivityModule(@NonNull String conversationId, boolean isShoutConversation) {
        this.conversationId = conversationId;
        mIsShoutConversation = isShoutConversation;
    }

    @Provides
    public String getConversationId() {
        return conversationId;
    }

    @Provides
    public boolean isShoutConversation() {
        return mIsShoutConversation;
    }
}
