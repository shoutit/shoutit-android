package com.shoutit.app.android.view.filter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dao.CategoriesDao;
import com.shoutit.app.android.dao.SortTypesDao;
import com.shoutit.app.android.view.search.SearchPresenter;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class FiltersFragmentModule extends FragmentModule {

    @Nonnull
    private final SearchPresenter.SearchType searchType;
    @Nullable
    private final String initCategorySlug;

    public FiltersFragmentModule(@Nonnull Fragment fragment,
                                 @Nonnull SearchPresenter.SearchType searchType,
                                 @Nullable String initCategorySlug) {
        super(fragment);
        this.searchType = searchType;
        this.initCategorySlug = initCategorySlug;
    }


    @Provides
    FilterPresenterFactory provideFilterPresenterFactory(CategoriesDao categoriesDao, SortTypesDao sortTypesDao,
                                             @UiScheduler Scheduler uiScheduler, @ForActivity Context context,
                                             UserPreferences userPreferences) {
        return new FilterPresenterFactory(categoriesDao, sortTypesDao, uiScheduler, context,
                userPreferences, searchType, initCategorySlug);
    }

}

