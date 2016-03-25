package com.shoutit.app.android.view.search.main.shouts;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ForActivity;
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

    public SearchShoutsFragmentModule(Fragment fragment,
                                      @Nonnull SearchPresenter.SearchType searchType) {
        super(fragment);
        this.searchType = searchType;
    }


    @Provides
    SearchPresenter provideSearchPresenter(ApiService apiService, @NetworkScheduler Scheduler networkScheduler,
                                           @UiScheduler Scheduler uiScheduler, SearchQueryPresenter searchQueryPresenter,
                                           @ForActivity Context context, UserPreferences userPreferences) {
        return new SearchPresenter(apiService, searchQueryPresenter, networkScheduler, uiScheduler, searchType, null, null, userPreferences, context);
    }

}

