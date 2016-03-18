package com.shoutit.app.android.view.search.subsearch;

import android.content.Context;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.SearchQueryPresenter;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class SubSearchActivityModule {


    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nonnull
    private final String contextualItemId;
    @Nonnull
    private final String contextualItemName;

    public SubSearchActivityModule(@Nonnull SearchPresenter.SearchType searchType,
                                   @Nullable String contextualItemId,
                                   @Nonnull String contextualItemName) {
        this.searchType = searchType;
        this.contextualItemId = contextualItemId;
        this.contextualItemName = contextualItemName;
    }

    @Provides
    SearchPresenter provideSearchPresenter(ApiService apiService, @NetworkScheduler Scheduler networkScheduler,
                                           @UiScheduler Scheduler uiScheduler, SearchQueryPresenter searchQueryPresenter,
                                           @ForActivity Context context) {
        return new SearchPresenter(apiService, searchQueryPresenter, networkScheduler,
                uiScheduler, searchType, contextualItemId, contextualItemName, context);
    }

    @Provides
    @ActivityScope
    SearchQueryPresenter provideSearchQueryPresenter() {
        return new SearchQueryPresenter();
    }

}


