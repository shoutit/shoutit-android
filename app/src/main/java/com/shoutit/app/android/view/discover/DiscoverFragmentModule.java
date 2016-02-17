package com.shoutit.app.android.view.discover;

import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dao.DiscoversDao;

import dagger.Module;
import dagger.Provides;

@Module
public class DiscoverFragmentModule {

    @Nullable
    private final String discoverId;

    public DiscoverFragmentModule(@Nullable String discoverId) {
        this.discoverId = discoverId;
    }

    @Provides
    public DiscoverPresenter provideDiscoverPresenter(UserPreferences userPreferences, DiscoversDao dao) {
        return new DiscoverPresenter(userPreferences, dao, Optional.fromNullable(discoverId));
    }
}
