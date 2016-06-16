package com.shoutit.app.android.view.promote;

import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dao.PromoteLabelsDao;
import com.shoutit.app.android.dao.PromoteOptionsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class PromoteActivityModule {

    @Nonnull
    private final String shoutId;
    @Nullable
    private final String shoutTitle;

    public PromoteActivityModule(@Nonnull String shoutId, @Nullable String shoutTitle) {
        this.shoutId = shoutId;
        this.shoutTitle = shoutTitle;
    }

    @Provides
    PromotePresenter providePromotePresenter(PromoteLabelsDao labelsDao, PromoteOptionsDao optionsDao,
                                             @UiScheduler Scheduler uiScheduler, @NetworkScheduler Scheduler networkScheduler,
                                             ShoutsGlobalRefreshPresenter shoutGlocalRefreshPresenter,
                                             UserPreferences userPreferences, ApiService apiService) {
        return new PromotePresenter(labelsDao, optionsDao, uiScheduler, networkScheduler, shoutGlocalRefreshPresenter,
                userPreferences, apiService, shoutTitle, shoutId);
    }
}
