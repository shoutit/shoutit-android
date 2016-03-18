package com.shoutit.app.android.view.search;

import com.shoutit.app.android.dagger.ActivityScope;


import dagger.Module;
import dagger.Provides;


@Module
public class BaseSearchActivityModule {

    public BaseSearchActivityModule() {
    }

    @Provides
    @ActivityScope
    SearchQueryPresenter provideSearchQueryPresenter() {
        return new SearchQueryPresenter();
    }
}

