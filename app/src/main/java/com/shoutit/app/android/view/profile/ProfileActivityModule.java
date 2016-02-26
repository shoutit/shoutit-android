package com.shoutit.app.android.view.profile;

import android.content.Context;

import com.appunite.rx.dagger.UiScheduler;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.ProfileKind;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.dao.ShoutsDao;

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
    public MyProfilePresenter provideProfilePresenter(ShoutsDao shoutsDao, @ForActivity Context context,
                                                      UserPreferences preferences, @UiScheduler Scheduler uiScheduler) {
        if (profileType.equals(ProfileKind.PROFILE)) {
            return new MyProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler);
        } else {
            // TODO
            return new MyProfilePresenter(userName, shoutsDao, context, preferences, uiScheduler);
        }
    }
}
