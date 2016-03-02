package com.shoutit.app.android.view.profile;

import android.content.Context;

import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ProfileType;
import com.shoutit.app.android.dagger.ActivityScope;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;
import com.shoutit.app.android.view.profile.myprofile.MyProfileAdapter;
import com.shoutit.app.android.view.profile.myprofile.MyProfilePresenter;
import com.shoutit.app.android.view.profile.userprofile.UserProfileAdapter;
import com.shoutit.app.android.view.profile.userprofile.UserProfilePresenter;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

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
                                                    UserPreferences preferences, @UiScheduler Scheduler uiScheduler,
                                                    @NetworkScheduler Scheduler networkScheduler, ApiService apiService) {
        final boolean isMyProfile = userName.equals(preferences.getUser().getUsername());
        if (isMyProfile && profileType.equalsIgnoreCase(ProfileType.USER)) {
            return new MyProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler, networkScheduler, apiService);
        } else {
            return new UserProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler, networkScheduler, apiService);
        }
    }

    @Provides
    @ActivityScope
    public ProfileAdapter provideProfileAdapter(@ForActivity Context context, UserPreferences preferences, Picasso picasso) {
        final boolean isMyProfile = userName.equals(preferences.getUser().getUsername());
        if (isMyProfile && profileType.equalsIgnoreCase(ProfileType.USER)) {
            return new MyProfileAdapter(context, picasso);
        } else {
            return new UserProfileAdapter(context, picasso);
        }
    }
}
