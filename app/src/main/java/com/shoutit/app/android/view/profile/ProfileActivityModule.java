package com.shoutit.app.android.view.profile;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.utils.PreferencesHelper;
import com.shoutit.app.android.view.profile.myprofile.MyProfileHalfPresenter;
import com.shoutit.app.android.view.profile.userprofile.UserProfileHalfPresenter;

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
                                                    PreferencesHelper preferencesHelper, UserProfileHalfPresenter userProfilePresenter,
                                                    MyProfileHalfPresenter myProfilePresenter) {
        return new ProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler,
                profilesDao, myProfilePresenter, userProfilePresenter, preferencesHelper);
    }
}
