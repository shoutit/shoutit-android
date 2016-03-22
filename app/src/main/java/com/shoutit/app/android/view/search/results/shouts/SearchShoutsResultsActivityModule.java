package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class SearchShoutsResultsActivityModule {

    private final String searchQuery;

    public SearchShoutsResultsActivityModule(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Provides
    SearchShoutsResultsPresenter provideSearchShoutsResultsPresenter(ShoutsDao dao, @ForActivity Context context,
                                                                     @UiScheduler Scheduler uiScheduler,
                                                                     UserPreferences userPreferences) {
        return new SearchShoutsResultsPresenter(dao, searchQuery, userPreferences, context, uiScheduler);
    }
}

