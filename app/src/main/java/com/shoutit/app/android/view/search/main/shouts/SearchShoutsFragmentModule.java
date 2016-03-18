package com.shoutit.app.android.view.search.main.shouts;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.SearchQueryPresenter;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class SearchShoutsFragmentModule extends FragmentModule {


    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nullable
    private final String contextualItemId;

    public SearchShoutsFragmentModule(Fragment fragment,
                                      @Nonnull SearchPresenter.SearchType searchType,
                                      @Nullable String contextualItemId) {
        super(fragment);
        this.searchType = searchType;
        this.contextualItemId = contextualItemId;
    }


    @Provides
    SearchPresenter provideSearchPresenter(ApiService apiService, @NetworkScheduler Scheduler networkScheduler,
                                           @UiScheduler Scheduler uiScheduler, SearchQueryPresenter searchQueryPresenter) {
        return new SearchPresenter(apiService, searchQueryPresenter, networkScheduler, uiScheduler, searchType, contextualItemId);
    }

}

