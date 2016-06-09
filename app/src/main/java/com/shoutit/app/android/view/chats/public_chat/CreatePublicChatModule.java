package com.shoutit.app.android.view.chats.public_chat;

import android.content.Context;

import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ImageCaptureHelper;

import dagger.Module;
import dagger.Provides;

@Module
public class CreatePublicChatModule {

    @ActivityScope
    @Provides
    public ImageCaptureHelper getImageCaptureHelper(@ForActivity Context context) {
        return new ImageCaptureHelper(context);
    }
}
