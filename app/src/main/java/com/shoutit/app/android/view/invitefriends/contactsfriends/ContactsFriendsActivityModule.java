package com.shoutit.app.android.view.invitefriends.contactsfriends;

import android.app.Activity;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;

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
    String providePlaceholderText() {
        return activity.getString(R.string.facebook_friends_no_friends);
    }

    @Provides
    @ActivityScope
    BaseProfileListPresenter provideProfilesListPresenter(@UiScheduler Scheduler uiScheduler,
                                                          PhoneContactsHelper phoneContactsHelper,
                                                          ProfilesDao dao,
                                                          @NetworkScheduler Scheduler networkScheduler,
                                                          ListeningHalfPresenter listeningHalfPresenter,
                                                          ApiService apiService,
                                                          UserPreferences userPreferences,
                                                          String placeholderText) {
        return new ContactsFriendsPresenter(phoneContactsHelper, apiService, networkScheduler,
                uiScheduler, dao, listeningHalfPresenter, userPreferences, placeholderText);
    }
}

