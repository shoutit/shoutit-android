package com.shoutit.app.android.view.filter;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;

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

    @Inject
    public FilterPresenterFactory(@Nonnull CategoriesDao categoriesDao,
                                  @Nonnull SortTypesDao sortTypesDao,
                                  @Nonnull @UiScheduler Scheduler uiScheduler,
                                  @Nonnull @ForActivity Context context,
                                  @Nonnull UserPreferences userPreferences) {
        this.categoriesDao = categoriesDao;
        this.sortTypesDao = sortTypesDao;
        this.uiScheduler = uiScheduler;
        this.context = context;
        this.userPreferences = userPreferences;
    }

    @Nonnull
    FiltersPresenter getFiltersPresenter() {
        return new FiltersPresenter(categoriesDao, sortTypesDao, uiScheduler, context, userPreferences);
    }
}
