package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.app.Activity;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.facebook.CallbackManager;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class FacebookFriendsActivityModule {

    private final Activity activity;

    public FacebookFriendsActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    ProfilesListPresenter provideProfilesListPresenter(@UiScheduler Scheduler uiScheduler,
                                                       FacebookHelper facebookHelper,
                                                       ProfilesDao dao,
                                                       UserPreferences userPreferences,
                                                       @NetworkScheduler Scheduler networkScheduler,
                                                       CallbackManager callbackManager,
                                                       ApiService apiService) {
        return new FacebookFriendsPresenter(facebookHelper, apiService, userPreferences,
                activity, callbackManager, dao, uiScheduler, networkScheduler);
    }

    @Provides
    @ActivityScope
    CallbackManager provideCallbackManager() {
        return CallbackManager.Factory.create();
    }
}

