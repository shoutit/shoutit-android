package com.shoutit.app.android.view.pages.my;


import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ListenersDaos;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class SelectListenersActivityModule {

    @Provides
    @ActivityScope
    BaseProfileListPresenter provideProfilesListPresenter(@UiScheduler Scheduler uiScheduler,
                                                          ListeningHalfPresenter listeningHalfPresenter,
                                                          UserPreferences userPreferences,
                                                          ListenersDaos listenersDaos) {
        return new SelectListenersPresenter(listeningHalfPresenter, uiScheduler,
                null, userPreferences, listenersDaos);
    }
}

