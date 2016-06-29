package com.shoutit.app.android.view.listeners;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

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
    BaseProfileListPresenter providesListenersPresenter(@UiScheduler Scheduler uiScheduler,
                                                        ListenersDaos dao,
                                                        ListeningHalfPresenter listeningHalfPresenter,
                                                        UserPreferences userPreferences) {
        return new ListenersPresenter(dao, uiScheduler, listeningHalfPresenter, userName, userPreferences);
    }
}
