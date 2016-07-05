package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.utils.FBAdHalfPresenter;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.view.search.SearchPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class SearchShoutsResultsFragmentModule {

    private final String searchQuery;
    @Nullable
    private final String contextualItemId;
    @Nonnull
    private final SearchPresenter.SearchType searchType;

    public SearchShoutsResultsFragmentModule(@Nullable String searchQuery,
                                             @Nullable String contextualItemId,
                                             @Nonnull SearchPresenter.SearchType searchType) {
        this.contextualItemId = contextualItemId;
        this.searchType = searchType;
        this.searchQuery = searchQuery;
    }

    @Provides
    SearchShoutsResultsPresenter provideSearchShoutsResultsPresenter(ShoutsDao dao, @ForActivity Context context,
                                                                     @UiScheduler Scheduler uiScheduler,
                                                                     UserPreferences userPreferences,
                                                                     ShoutsGlobalRefreshPresenter globalRefreshPresenter,
                                                                     FBAdHalfPresenter fbAdHalfPresenter,
                                                                     BookmarksDao bookmarksDao,
                                                                     BookmarkHelper bookmarkHelper) {
        return new SearchShoutsResultsPresenter(dao, searchQuery, searchType, contextualItemId,
                userPreferences, context, uiScheduler, fbAdHalfPresenter, globalRefreshPresenter, bookmarksDao, bookmarkHelper);
    }
}

