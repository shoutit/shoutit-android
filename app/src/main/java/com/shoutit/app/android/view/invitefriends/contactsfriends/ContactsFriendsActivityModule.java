package com.shoutit.app.android.view.invitefriends.contactsfriends;

import android.app.Activity;
import android.content.res.Resources;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.view.profileslist.ProfilesListPresenter;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;


@Module
public class ContactsFriendsActivityModule {

    private final Activity activity;

    public ContactsFriendsActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    ProfilesListPresenter provideProfilesListPresenter(@UiScheduler Scheduler uiScheduler,
                                                       PhoneContactsHelper phoneContactsHelper,
                                                       ProfilesDao dao,
                                                       @ForActivity Resources resources,
                                                       @NetworkScheduler Scheduler networkScheduler,
                                                       ListeningHalfPresenter listeningHalfPresenter,
                                                       ApiService apiService,
                                                       UserPreferences userPreferences,
                                                       PreferencesHelper preferencesHelper) {
        return new ContactsFriendsPresenter(phoneContactsHelper, apiService, networkScheduler,
                uiScheduler, resources, dao, listeningHalfPresenter, userPreferences, preferencesHelper);
    }
}

