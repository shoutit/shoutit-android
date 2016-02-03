package com.shoutit.app.android.view.main;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.functions.Functions1;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.model.Location;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.utils.TextHelper;

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

    @Inject
    public MenuHandlerPresenter(@Nonnull UserPreferences userPreferences) {

        final Observable<User> userObservable = userPreferences.userObservable()
                .compose(ObservableExtensions.<User>behaviorRefCount());

        avatarObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return TextHelper.emptyToNull(user.getImage());
                    }
                });

        coverObservable = userObservable
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return TextHelper.emptyToNull(user.getCover());
                    }
                });

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
}
