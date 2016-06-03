package com.shoutit.app.android.view.invitefriends.facebookfriends;

import android.app.Activity;

import com.appunite.rx.dagger.UiScheduler;
import com.facebook.CallbackManager;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.listenings.ProfilesListAdapter;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;
import com.squareup.picasso.Picasso;

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
                                                       CallbackManager callbackManager,
                                                       ListeningHalfPresenter listeningHalfPresenter) {
        return new FacebookFriendsPresenter(facebookHelper, userPreferences,
                activity, callbackManager, dao, uiScheduler, listeningHalfPresenter);
    }

    @Provides
    @ActivityScope
    CallbackManager provideCallbackManager() {
        return CallbackManager.Factory.create();
    }

    @Provides
    ProfilesListAdapter provideProfilesListAdapter(Picasso picasso) {
        return new FacebookFriendsAdapter(activity, picasso);
    }
}

