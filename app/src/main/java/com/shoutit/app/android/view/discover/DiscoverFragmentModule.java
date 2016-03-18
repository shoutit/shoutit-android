package com.shoutit.app.android.view.discover;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.dao.DiscoverShoutsDao;
import com.shoutit.app.android.dao.DiscoversDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class DiscoverFragmentModule extends FragmentModule {

    private final DiscoverFragment discoverFragment;
    @Nullable
    private final String discoverId;

    public DiscoverFragmentModule(DiscoverFragment discoverFragment, @Nullable String discoverId) {
        super(discoverFragment);
        this.discoverFragment = discoverFragment;
        this.discoverId = discoverId;
    }

    @Provides
    @FragmentScope
    public DiscoverPresenter provideDiscoverPresenter(UserPreferences userPreferences,
                                                      DiscoversDao dao, DiscoverShoutsDao discoverShoutsDao,
                                                      @UiScheduler Scheduler uiScheduler,
                                                      @NetworkScheduler Scheduler networkScheduler,
                                                      @ForActivity Resources resources,
                                                      @ForActivity Context context) {
        return new DiscoverPresenter(userPreferences, dao, discoverShoutsDao,
                Optional.fromNullable(discoverId), uiScheduler, networkScheduler, resources, context);
    }


    @Provides
    OnNewDiscoverSelectedListener provideOnNewDiscoverSelectedListener() {
        return (OnNewDiscoverSelectedListener) discoverFragment.getActivity();
    }

}
