package com.shoutit.app.android.view.videoconversation;

import com.appunite.rx.dagger.NetworkScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.VideoCallsDao;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class VideoConversationModule {
    @Nonnull
    private final String username;

    public VideoConversationModule(VideoConversationActivity videoConversationActivity,
                                   @Nonnull String username) {
        this.username = username;
    }

    @Provides
    String provideUsername(){return username;}

    @Provides
    public VideoCallsDao provideVideoCallsDao(ApiService apiService,
                                              @NetworkScheduler Scheduler networkScheduler) {
        return new VideoCallsDao(apiService, networkScheduler);
    }
}
