package com.shoutit.app.android.view.admins;

import android.support.v4.app.Fragment;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.dagger.FragmentScope;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;


import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class AdminsFragmentModule extends FragmentModule {

    public AdminsFragmentModule(Fragment fragment) {
        super(fragment);
    }

    @Provides
    @FragmentScope
    BaseProfileListPresenter profileListPresenter(ListeningHalfPresenter listeningHalfPresenter,
                                                  ProfilesDao profilesDao, @UiScheduler Scheduler uiScheduler,
                                                  @NetworkScheduler Scheduler networkScheduler,
                                                  UserPreferences userPreferences,
                                                  ApiService apiService) {
        return new AdminsFragmentPresenter(listeningHalfPresenter, profilesDao, null, uiScheduler,
                networkScheduler, userPreferences, apiService);
    }
}
