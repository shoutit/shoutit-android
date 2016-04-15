package com.shoutit.app.android.view.chats.chats_first_conversation;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatsFirstConversationActivityModule {

    private final boolean mIsShoutConversation;
    @NonNull
    private final String mIdForCreation;

    public ChatsFirstConversationActivityModule(boolean isShoutConversation, @NonNull String idForCreation) {
        mIsShoutConversation = isShoutConversation;
        mIdForCreation = idForCreation;
    }

    @Provides
    public boolean isShoutConversation() {
        return mIsShoutConversation;
    }

    @Provides
    public String getIdForCreation() {
        return mIdForCreation;
    }
}
