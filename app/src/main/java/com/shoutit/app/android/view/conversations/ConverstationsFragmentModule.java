package com.shoutit.app.android.view.conversations;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;
import com.shoutit.app.android.view.chats.LocalMessageBus;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ConverstationsFragmentModule {

    private final boolean isMyConversations;

    public ConverstationsFragmentModule(boolean isMyConversations) {
        this.isMyConversations = isMyConversations;
    }

    @Provides
    ConversationsPresenter provideConversationsPresenter(ApiService apiService, UserPreferences userPreferences,
                                                         @NetworkScheduler Scheduler networkScheduler,
                                                         @UiScheduler Scheduler uiScheduler,
                                                         @ForActivity Context context,
                                                         PusherHelperHolder pusherHelper,
                                                         LocalMessageBus bus,
                                                         RefreshConversationBus refreshConversationBus) {
        return new ConversationsPresenter(apiService, networkScheduler, uiScheduler, context,
                userPreferences, pusherHelper, isMyConversations, bus, refreshConversationBus);
    }
}
