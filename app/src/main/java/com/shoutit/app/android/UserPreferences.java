package com.shoutit.app.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class UserPreferences {

    private static final String AUTH_TOKEN = "token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER = "user";
    private static final String KEY_LOCATION = "location";
    private static final String IS_GUEST = "is_guest";
    private static final String KEY_LOCATION_TRACKING = "location_tracking";
    private static final String IS_FIRST_RUN = "is_first_run";

    private final PublishSubject<Object> userRefreshSubject = PublishSubject.create();
    private final PublishSubject<Object> locationRefreshSubject = PublishSubject.create();
    private final Observable<User> userObservable;
    private final Observable<UserLocation> locationObservable;

    @SuppressLint("CommitPrefEdits")
    private final SharedPreferences mPreferences;
    @Nonnull
    private final Gson gson;

    @Inject
    public UserPreferences(@ForApplication Context context, @Nonnull Gson gson) {
        this.gson = gson;
        mPreferences = context.getSharedPreferences("prefs", 0);

        locationObservable = Observable
                .defer(new Func0<Observable<UserLocation>>() {
                    @Override
                    public Observable<UserLocation> call() {
                        return Observable.just(getLocation());
                    }
                })
                .compose(MoreOperators.<UserLocation>refresh(locationRefreshSubject))
                .filter(Functions1.isNotNull());

        userObservable = Observable
                .defer(new Func0<Observable<User>>() {
                    @Override
                    public Observable<User> call() {
                        return Observable.just(getUser());
                    }
                })
                .compose(MoreOperators.<User>refresh(userRefreshSubject))
                .filter(Functions1.isNotNull());
    }

    @SuppressLint("CommitPrefEdits")
    public void setLoggedIn(@NonNull String authToken, @NonNull String refreshToken) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(AUTH_TOKEN, authToken);
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.commit();
    }

    @SuppressLint("CommitPrefEdits")
    public void setGuest(boolean isGuest) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(IS_GUEST, isGuest);
        editor.commit();
    }

    public boolean isGuest() {
        return mPreferences.getBoolean(IS_GUEST, false);
    }

    public Optional<String> getAuthToken() {
        return Optional.fromNullable(mPreferences.getString(AUTH_TOKEN, null));
    }

    @SuppressLint("CommitPrefEdits")
    public void saveUserAsJson(User user) {
        mPreferences.edit()
                .putString(KEY_USER, gson.toJson(user))
                .commit();
        refreshUser();
    }

    public boolean isUserLoggedIn() {
        return getAuthToken().isPresent();
    }

    public boolean isFirstRun() {
        return mPreferences.getBoolean(IS_FIRST_RUN, true);
    }

    public boolean isFirstRunAndSetToFalse() {
        final boolean isFirstRun = mPreferences.getBoolean(IS_FIRST_RUN, true);
        mPreferences.edit().putBoolean(IS_FIRST_RUN, false).apply();
        return isFirstRun;
    }

    @Nullable
    public User getUser() {
        final String userJson = mPreferences.getString(KEY_USER, null);
        return gson.fromJson(userJson, User.class);
    }

    @Nonnull
    public Observable<User> userObservable() {
        return userObservable;
    }

    private void refreshUser() {
        userRefreshSubject.onNext(null);
    }

    @Nullable
    public String getUserCountryCode() {
        final UserLocation location = getLocation();
        return location != null ? location.getCountry() : null;
    }

    @Nullable
    public String getUserCity() {
        final UserLocation location = getLocation();
        return location != null ? location.getCity() : null;
    }

    @Nullable
    public UserLocation getLocation() {
        final String locationJson = mPreferences.getString(KEY_LOCATION, null);
        return gson.fromJson(locationJson, UserLocation.class);
    }

    public Observable<UserLocation> getLocationObservable() {
        return locationObservable;
    }

    public void saveLocation(@Nullable UserLocation location) {
        if (location == null) {
            return;
        }
        mPreferences.edit()
                .putString(KEY_LOCATION, gson.toJson(location))
                .commit();
        refreshLocation();
    }

    private void refreshLocation() {
        locationRefreshSubject.onNext(null);
    }

    public boolean automaticLocationTrackingEnabled() {
        return mPreferences.getBoolean(KEY_LOCATION_TRACKING, true);
    }

    @SuppressLint("CommitPrefEdits")
    public void setAutomaticLocationTrackingEnabled(boolean enable) {
        mPreferences.edit()
                .putBoolean(KEY_LOCATION_TRACKING, enable)
                .commit();
    }

    @SuppressLint("CommitPrefEdits")
    public void logout() {
        mPreferences.edit().clear().commit();
    }
}
