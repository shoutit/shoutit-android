package com.shoutit.app.android.view.profile.page.edit;

import android.support.annotation.NonNull;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.Page;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class PageDataProviderFactory {

    private final UserPreferences mUserPreferences;
    private final ApiService mApiService;
    private final Scheduler uiScheduler;
    private final Scheduler networkScheduler;

    @Inject
    public PageDataProviderFactory(UserPreferences userPreferences,
                                   ApiService apiService,
                                   @UiScheduler Scheduler uiScheduler,
                                   @NetworkScheduler Scheduler networkScheduler) {
        mUserPreferences = userPreferences;
        mApiService = apiService;
        this.uiScheduler = uiScheduler;
        this.networkScheduler = networkScheduler;
    }

    public PageDataProvider createLocalPageDataProvider() {
        return () -> {
            final Page userOrPage = (Page) mUserPreferences.getUserOrPage();
            return Observable.just(userOrPage);
        };
    }

    public PageDataProvider createRemotePageDataProvider(@NonNull String username) {
        return () -> mApiService.getUser(username)
                .observeOn(uiScheduler)
                .subscribeOn(networkScheduler)
                .map(baseProfile -> (Page) baseProfile);
    }
}
