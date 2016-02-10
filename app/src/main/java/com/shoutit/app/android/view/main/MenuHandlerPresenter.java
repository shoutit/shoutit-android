package com.shoutit.app.android.view.main;

import android.content.Context;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.functions.Functions1;
import com.google.common.base.Strings;
import com.shoutit.app.android.BuildConfig;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Location;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.utils.ResourcesHelper;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

public class MenuHandlerPresenter {

    @Nonnull
    private final Observable<String> avatarObservable;
    @Nonnull
    private final Observable<String> coverObservable;
    @Nonnull
    private final Observable<String> nameObservable;
    @Nonnull
    private final Observable<String> locationObservable;
    @Nonnull
    private final Observable<Integer> countryCodeObservable;
    @Nonnull
    private final Observable<String> versionNameObservable;
    @Nonnull
    private final Observable<Boolean> logoutItemVisibilityObservable;

    @Inject
    public MenuHandlerPresenter(@Nonnull UserPreferences userPreferences,
                                @Nonnull @ForActivity final Context context) {

        final Observable<User> userObservable = userPreferences.userObservable()
                .compose(ObservableExtensions.<User>behaviorRefCount());

        avatarObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return Strings.emptyToNull(user.getImage());
                    }
                });

        coverObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return Strings.emptyToNull(user.getCover());
                    }
                })
                .filter(Functions1.isNotNull());

        nameObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.getName();
                    }
                });

        locationObservable = userObservable
                .map(new Func1<User, Location>() {
                    @Override
                    public Location call(User user) {
                        return user.getLocation();
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<Location, String>() {
                    @Override
                    public String call(Location location) {
                        return location.getCity();
                    }
                });

        countryCodeObservable = userObservable
                .map(new Func1<User, Location>() {
                    @Override
                    public Location call(User user) {
                        return user.getLocation();
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<Location, String>() {
                    @Override
                    public String call(Location location) {
                        return Strings.emptyToNull(location.getCountry());
                    }
                })
                .filter(Functions1.isNotNull())
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String countryCode) {
                        return ResourcesHelper.getResourceIdForName(countryCode.toLowerCase(), context);
                    }
                })
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer != 0;
                    }
                });

        versionNameObservable = Observable.just(
                context.getString(R.string.menu_version_name, BuildConfig.VERSION_NAME)
        );

        logoutItemVisibilityObservable = Observable.just(userPreferences.isUserLoggedIn());
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
    public Observable<String> getLocationObservable() {
        return locationObservable;
    }

    @Nonnull
    public Observable<Integer> getCountryCodeObservable() {
        return countryCodeObservable;
    }

    @Nonnull
    public Observable<String> getVersionNameObservable() {
        return versionNameObservable;
    }

    @Nonnull
    public Observable<Boolean> getLogoutItemVisibilityObservable() {
        return logoutItemVisibilityObservable;
    }
}
