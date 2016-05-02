package com.shoutit.app.android.view.shouts.discover;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.DiscoverShoutsDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class DiscoverShoutsActivityModule {

    private final String discoveryId;
    private final String name;

    public DiscoverShoutsActivityModule(String discoveryId, String name) {
        this.discoveryId = discoveryId;
        this.name = name;
    }

    @Provides
    public DiscoverShoutsPresenter provideDiscoverShoutsPresenter(@NetworkScheduler Scheduler networkScheduler,
                                                                  @UiScheduler Scheduler uiScheduler,
                                                                  DiscoverShoutsDao dao,
                                                                  @ForActivity Context context,
                                                                  UserPreferences userPreferences) {
        return new DiscoverShoutsPresenter(networkScheduler, uiScheduler, dao, discoveryId, name, userPreferences, context);
    }
}
