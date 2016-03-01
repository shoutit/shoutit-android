package com.shoutit.app.android.view.profile;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.view.profile.myprofile.MyProfileAdapter;
import com.shoutit.app.android.view.profile.myprofile.MyProfilePresenter;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

@Module
public class ProfileActivityModule {

    @Nonnull
    private final String userName;
    @Nonnull
    private final String profileType;

    public ProfileActivityModule(@Nonnull String userName, @Nonnull String profileType) {
        this.userName = userName;
        this.profileType = profileType;
    }

    @Provides
    @ActivityScope
    public ProfilePresenter provideProfilePresenter(ShoutsDao shoutsDao, @ForActivity Context context,
                                                      UserPreferences preferences, @UiScheduler Scheduler uiScheduler) {
        final boolean isMyProfile = userName.equals(preferences.getUser().getUsername());
        if (isMyProfile && profileType.equalsIgnoreCase(ProfileType.PROFILE)) {
            return new MyProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler);
        } else {
            // TODO
            return new MyProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler);
        }
    }

    @Provides
    @ActivityScope
    public ProfileAdapter provideProfileAdapter(@ForActivity Context context, UserPreferences preferences, Picasso picasso) {
        final boolean isMyProfile = userName.equals(preferences.getUser().getUsername());
        if (isMyProfile && profileType.equalsIgnoreCase(ProfileType.PROFILE)) {
            return new MyProfileAdapter(context, picasso);
        } else {
            // TODO
            return new MyProfileAdapter(context, picasso);
        }
    }
}
