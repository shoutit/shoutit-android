package com.shoutit.app.android.view.listeners;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class ListenersActivityModule {

    @Nonnull
    private final String userName;

    public ListenersActivityModule(@Nonnull String userName) {
        this.userName = userName;
    }

    @Provides
    @ActivityScope
    ProfilesListPresenter providesListenersPresenter(@UiScheduler Scheduler uiScheduler,
                                                     ListenersDaos dao,
                                                     @NetworkScheduler Scheduler networkScheduler,
                                                     ApiService apiService) {
        return new ListenersPresenter(dao, apiService, uiScheduler, networkScheduler, userName);
    }
}
