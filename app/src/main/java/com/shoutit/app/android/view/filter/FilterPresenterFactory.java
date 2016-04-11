package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.view.search.SearchPresenter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Scheduler;

public class FilterPresenterFactory {

    @Nonnull
    private final CategoriesDao categoriesDao;
    @Nonnull
    private final SortTypesDao sortTypesDao;
    @Nonnull
    private final Scheduler uiScheduler;
    @Nonnull
    private final Context context;
    @Nonnull
    private final UserPreferences userPreferences;
    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nullable
    private final String initCategorySlug;

    @Inject
    public FilterPresenterFactory(@Nonnull CategoriesDao categoriesDao,
                                  @Nonnull SortTypesDao sortTypesDao,
                                  @Nonnull @UiScheduler Scheduler uiScheduler,
                                  @Nonnull @ForActivity Context context,
                                  @Nonnull UserPreferences userPreferences,
                                  @Nonnull SearchPresenter.SearchType searchType,
                                  @Nullable String initCategorySlug) {
        this.categoriesDao = categoriesDao;
        this.sortTypesDao = sortTypesDao;
        this.uiScheduler = uiScheduler;
        this.context = context;
        this.userPreferences = userPreferences;
        this.searchType = searchType;
        this.initCategorySlug = initCategorySlug;
    }

    @Nonnull
    FiltersPresenter getFiltersPresenter() {
        return new FiltersPresenter(categoriesDao, sortTypesDao, uiScheduler,
                context, userPreferences, searchType, initCategorySlug);
    }
}
