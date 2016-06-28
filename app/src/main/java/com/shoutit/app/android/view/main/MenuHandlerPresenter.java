package com.shoutit.app.android.view.main;

import android.content.Context;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ResourcesHelper;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;

public class MenuHandlerPresenter {

    @Nonnull
    private final Observable<String> avatarObservable;
    @Nonnull
    private final Observable<String> coverObservable;
    @Nonnull
    private final Observable<String> nameObservable;
    @Nonnull
    private final Observable<String> cityObservable;
    @Nonnull
    private final Observable<Integer> countryCodeObservable;
    @Nonnull
    private final Observable<String> versionNameObservable;
    private final Observable<String> userNameObservable;

    @Inject
    public MenuHandlerPresenter(@Nonnull final UserPreferences userPreferences,
                                @Nonnull @ForActivity final Context context,
                                @Nonnull @UiScheduler Scheduler uiScheduler) {

        final Observable<BaseProfile> userOrPageObservable = userPreferences.getPageOrUserObservable()
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<BaseProfile>behaviorRefCount());

        avatarObservable = userOrPageObservable
                .map(user -> Strings.emptyToNull(user.getImage()));

        coverObservable = userOrPageObservable
                .map(user -> Strings.emptyToNull(user.getCover()))
                .filter(Functions1.isNotNull());

        nameObservable = userOrPageObservable
                .map(user -> {
                    if (userPreferences.isGuest()) {
                        return context.getString(R.string.menu_guest);
                    } else {
                        return user.getName();
                    }
                });

        userNameObservable = Observable.just(userPreferences.getUser().getUsername());

        final Observable<UserLocation> locationObservable = userPreferences.getLocationObservable()
                .observeOn(uiScheduler)
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());

        cityObservable = locationObservable
                .map(UserLocation::getCity);

        countryCodeObservable = locationObservable
                .map(location -> Strings.emptyToNull(location.getCountry()))
                .filter(Functions1.isNotNull())
                .map(countryCode -> ResourcesHelper.getResourceIdForName(countryCode.toLowerCase(), context))
                .filter(integer -> integer != 0);

        versionNameObservable = Observable.just(
                context.getString(R.string.menu_version_name, BuildConfig.VERSION_NAME)
        );
    }

    @Nonnull
    public Observable<String> getAvatarObservable() {
        return avatarObservable;
    }

    @Nonnull
    public Observable<String> getCoverObservable() {
        return coverObservable;
    }

    @Nonnull
    public Observable<String> getNameObservable() {
        return nameObservable;
    }

    @Nonnull
    public Observable<String> getUserNameObservable() {
        return userNameObservable;
    }

    @Nonnull
    public Observable<String> getCityObservable() {
        return cityObservable;
    }

    @Nonnull
    public Observable<Integer> getCountryCodeObservable() {
        return countryCodeObservable;
    }

    @Nonnull
    public Observable<String> getVersionNameObservable() {
        return versionNameObservable;
    }
}
