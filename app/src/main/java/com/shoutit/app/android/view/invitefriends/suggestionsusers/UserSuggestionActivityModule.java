package com.shoutit.app.android.view.invitefriends.suggestionsusers;

import android.app.Activity;
import android.content.res.Resources;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class UserSuggestionActivityModule {

    private final Activity activity;

    public UserSuggestionActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    ProfilesListPresenter provideProfilesListPresenter(@UiScheduler Scheduler uiScheduler,
                                                       ProfilesDao profilesDao,
                                                       @ForActivity Resources resources,
                                                       ListeningHalfPresenter listeningHalfPresenter,
                                                       UserPreferences userPreferences) {
        return new UserSuggestionPresenter(profilesDao, uiScheduler, resources, listeningHalfPresenter, userPreferences);
    }
}
