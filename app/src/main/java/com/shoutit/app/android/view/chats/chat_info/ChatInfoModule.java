package com.shoutit.app.android.view.chats.chat_info;

import android.content.Context;
import android.support.annotation.NonNull;

import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ImageCaptureHelper;

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

    @ActivityScope
    @Provides
    public ImageCaptureHelper provideImageCaptureHelper(@ForActivity Context context) {
        return new ImageCaptureHelper(context);
    }
}