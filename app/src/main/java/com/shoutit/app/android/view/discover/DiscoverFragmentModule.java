package com.shoutit.app.android.view.discover;

import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class DiscoverFragmentModule extends FragmentModule {

    @Nullable
    private final String discoverId;

    public DiscoverFragmentModule(DiscoverFragment discoverFragment, @Nullable String discoverId) {
        super(discoverFragment);
        this.discoverId = discoverId;
    }

    @Provides
    public DiscoverPresenter provideDiscoverPresenter(UserPreferences userPreferences,
                                                      DiscoversDao dao, DiscoverShoutsDao discoverShoutsDao,
                                                      @UiScheduler Scheduler uiScheduler,
                                                      @NetworkScheduler Scheduler networkScheduler) {
        return new DiscoverPresenter(userPreferences, dao, discoverShoutsDao, Optional.fromNullable(discoverId), uiScheduler, networkScheduler);
    }
}
