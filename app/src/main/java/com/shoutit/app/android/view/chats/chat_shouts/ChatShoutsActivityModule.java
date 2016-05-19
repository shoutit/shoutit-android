package com.shoutit.app.android.view.chats.chat_shouts;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ChatShoutsActivityModule {

    @Nonnull
    private final String conversationId;

    public ChatShoutsActivityModule(@Nonnull String conversationId) {
        this.conversationId = conversationId;
    }

    @Provides
    ChatShoutsPresenter provideChatShoutsPresenter(@UiScheduler Scheduler uiScheduler,
                                                   ShoutsDao shoutsDao, @ForActivity Context context,
                                                   UserPreferences userPreferences) {
        return new ChatShoutsPresenter(uiScheduler, shoutsDao, conversationId, context, userPreferences);
    }
}
