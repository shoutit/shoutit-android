package com.shoutit.app.android.view.profile.user;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.BookmarksDao;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.dao.ShoutsGlobalRefreshPresenter;
import com.shoutit.app.android.utils.BookmarkHelper;
import com.shoutit.app.android.utils.ListeningHalfPresenter;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.view.profile.user.myprofile.MyProfileHalfPresenter;
import com.shoutit.app.android.view.profile.user.userprofile.UserProfileHalfPresenter;


import javax.annotation.Nonnull;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ProfileActivityModule {

    @Nonnull
    private final String userName;

    public ProfileActivityModule(@Nonnull String userName) {
        this.userName = userName;
    }

    @Provides
    @ActivityScope
    public ProfilePresenter provideProfilePresenter(ShoutsDao shoutsDao, ProfilesDao profilesDao, @ForActivity Context context,
                                                    UserPreferences preferences, @UiScheduler Scheduler uiScheduler,
                                                    @NetworkScheduler Scheduler networkScheduler, ApiService apiService,
                                                    PreferencesHelper preferencesHelper, UserProfileHalfPresenter userProfilePresenter,
                                                    MyProfileHalfPresenter myProfilePresenter, ShoutsGlobalRefreshPresenter shoutsGlobalRefreshPresenter,
                                                    PusherHelper pusherHelper, ListeningHalfPresenter listeningHalfPresenter, BookmarksDao bookmarksDao,
                                                    BookmarkHelper bookmarkHelper) {
        return new UserProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler, networkScheduler,
                profilesDao, myProfilePresenter, userProfilePresenter, preferencesHelper, shoutsGlobalRefreshPresenter,
                listeningHalfPresenter, apiService, pusherHelper, bookmarksDao, bookmarkHelper);
    }

}