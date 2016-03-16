package com.shoutit.app.android.view.search;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.db.SuggestionsTable;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class SearchFragmentModule extends FragmentModule {


    @Nonnull
    private final SearchPresenter.SearchType searchType;
    private final boolean displaySuggestions;
    @Nullable
    private final String contextualItemId;

    public SearchFragmentModule(Fragment fragment,
                                @Nonnull SearchPresenter.SearchType searchType,
                                boolean displaySuggestions,
                                @Nullable String contextualItemId) {
        super(fragment);
        this.searchType = searchType;
        this.displaySuggestions = displaySuggestions;
        this.contextualItemId = contextualItemId;
    }

    @Provides
    SearchPresenter provideSearchPresenter(ApiService apiService, SuggestionsTable suggestionsTable,
                                           @ForActivity Context context, @NetworkScheduler Scheduler networkScheduler,
                                           @UiScheduler Scheduler uiScheduler) {
        return new SearchPresenter(apiService, suggestionsTable, context, networkScheduler,
                uiScheduler, searchType, contextualItemId, displaySuggestions);
    }

}

