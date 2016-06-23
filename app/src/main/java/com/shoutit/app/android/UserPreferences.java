package com.shoutit.app.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.operators.MoreOperators;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.model.Stats;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class UserPreferences {

    private static final String AUTH_TOKEN = "token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER = "user";
    private static final String KEY_GUEST_USER = "guest_user";
    private static final String KEY_LOCATION = "location";
    private static final String IS_GUEST = "is_guest";
    private static final String KEY_LOCATION_TRACKING = "location_tracking";
    private static final String SHOULD_ASK_FOR_INTEREST = "is_first_run";
    private static final String GCM_PUSH_TOKEN = "gcm_push_token";
    private static final String TWILIO_TOKEN = "twilio_token";
    private static final String KEY_PROFILE_ALERT_DISPLAYED = "profile_alert_displayed";
    private static final String KEY_WAS_SHARE_DIALOG_DISPLAYED = "was_share_info_dialog_displayed";

    private final PublishSubject<Object> userRefreshSubject = PublishSubject.create();
    private final PublishSubject<Object> locationRefreshSubject = PublishSubject.create();
    private final PublishSubject<Object> tokenRefreshSubject = PublishSubject.create();
    private final Observable<User> userObservable;
    // locationObservable should be used instead userObservable to get location as there is no user for guest
    private final Observable<UserLocation> locationObservable;
    private final Observable<String> tokenObservable;

    @SuppressLint("CommitPrefEdits")
    private final SharedPreferences mPreferences;
    @Nonnull
    private final Gson gson;

    @Inject
    public UserPreferences(@ForApplication Context context, @Nonnull Gson gson, @UiScheduler Scheduler uiScheduler) {
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
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        userObservable = Observable
                .defer(new Func0<Observable<User>>() {
                    @Override
                    public Observable<User> call() {
                        return Observable.just(getUser());
                    }
                })
                .compose(MoreOperators.<User>refresh(userRefreshSubject))
                .observeOn(uiScheduler);

        tokenObservable = Observable
                .defer(new Func0<Observable<Optional<String>>>() {
                    @Override
                    public Observable<Optional<String>> call() {
                        return Observable.just(getAuthToken());
                    }
                })
                .filter(new Func1<Optional<String>, Boolean>() {
                    @Override
                    public Boolean call(Optional<String> stringOptional) {
                        return stringOptional.isPresent();
                    }
                })
                .map(new Func1<Optional<String>, String>() {
                    @Override
                    public String call(Optional<String> stringOptional) {
                        return stringOptional.get();
                    }
                })
                .compose(MoreOperators.<String>refresh(tokenRefreshSubject))
                .observeOn(uiScheduler);
    }

    @SuppressLint("CommitPrefEdits")
    public void setLoggedIn(@NonNull String authToken,
                            @NonNull String refreshToken,
                            @Nonnull User user) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor
                .putString(AUTH_TOKEN, authToken)
                .putString(REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER, gson.toJson(user))
                .putBoolean(IS_GUEST, false);
        editor.commit();
        tokenRefreshSubject.onNext(new Object());
        refreshUser();
        if (user.getLocation() != null) {
            saveLocation(user.getLocation());
        }
    }

    @SuppressLint("CommitPrefEdits")
    public void setGuestLoggedIn(@Nonnull User user, @NonNull String authToken, @NonNull String refreshToken) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        final String guestUser = gson.toJson(user, User.class);

        editor
                .putString(KEY_GUEST_USER, guestUser)
                .putString(AUTH_TOKEN, authToken)
                .putString(REFRESH_TOKEN, refreshToken)
                .putBoolean(IS_GUEST, true);
        editor.commit();
        tokenRefreshSubject.onNext(new Object());
    }

    @SuppressLint("CommitPrefEdits")
    public void setGuest(boolean isGuest) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(IS_GUEST, isGuest);
        editor.commit();
    }

    @Nullable
    public User getGuestUser() {
        final String guestUserJson = mPreferences.getString(KEY_GUEST_USER, null);
        return gson.fromJson(guestUserJson, User.class);
    }

    public boolean isGuest() {
        return mPreferences.getBoolean(IS_GUEST, false);
    }

    public boolean isNormalUser() {
        return !isGuest() && isUserLoggedIn();
    }

    public Optional<String> getAuthToken() {
        return Optional.fromNullable(mPreferences.getString(AUTH_TOKEN, null));
    }

    @SuppressLint("CommitPrefEdits")
    public void updateUserJson(User user) {
        if (isNormalUser()) {
            mPreferences.edit()
                    .putString(KEY_USER, gson.toJson(user))
                    .commit();
            refreshUser();
        }

        if (user.getLocation() != null) {
            saveLocation(user.getLocation());
        }
    }

    public boolean isUserLoggedIn() {
        return getAuthToken().isPresent();
    }

    public void setShouldAskForInterestTrue() {
        mPreferences.edit().putBoolean(SHOULD_ASK_FOR_INTEREST, true).apply();
    }

    public void setGcmPushToken(String gcmPushToken) {
        mPreferences.edit().putString(GCM_PUSH_TOKEN, gcmPushToken).apply();
    }

    @Nullable
    public String getGcmPushToken() {
        return mPreferences.getString(GCM_PUSH_TOKEN, null);
    }

    public boolean shouldAskForInterestAndSetToFalse() {
        final boolean isFirstRun = mPreferences.getBoolean(SHOULD_ASK_FOR_INTEREST, false);
        mPreferences.edit().putBoolean(SHOULD_ASK_FOR_INTEREST, false).apply();
        return isFirstRun;
    }

    @Nullable
    public User getUser() {
        final String userJson = mPreferences.getString(KEY_USER, null);
        return gson.fromJson(userJson, User.class);
    }

    @NonNull
    public User getUserOrThrow() {
        return Preconditions.checkNotNull(getUser());
    }

    @Nonnull
    public Observable<User> getUserObservable() {
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

    public Observable<String> getTokenObservable() {
        return tokenObservable;
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
        final String locationJson = mPreferences.getString(KEY_LOCATION, null);
        mPreferences.edit()
                .clear()
                .putString(KEY_LOCATION, locationJson)
                .commit();
    }

    @SuppressLint("CommitPrefEdits")
    public void setTwilioToken(@Nullable String twilioToken) {
        mPreferences.edit()
                .putString(TWILIO_TOKEN, twilioToken)
                .commit();
    }

    @Nullable
    public String getTwilioToken() {
        return mPreferences.getString(TWILIO_TOKEN, null);
    }

    public boolean wasProfileAlertAlreadyDisplayed() {
        return mPreferences.getBoolean(KEY_PROFILE_ALERT_DISPLAYED, false);
    }

    @SuppressLint("CommitPrefEdits")
    public void setProfileAlertAlreadyDisplayed() {
        mPreferences.edit()
                .putBoolean(KEY_PROFILE_ALERT_DISPLAYED, true)
                .commit();
    }

    public boolean wasShareDialogAlreadyDisplayed() {
        return mPreferences.getBoolean(KEY_WAS_SHARE_DIALOG_DISPLAYED, false);
    }

    @SuppressLint("CommitPrefEdits")
    public void setShareDialogDisplayed() {
        mPreferences.edit()
                .putBoolean(KEY_WAS_SHARE_DIALOG_DISPLAYED, true)
                .commit();
    }

    public void updateStats(@Nonnull Stats pusherStats) {
        final User user = getUser();
        if (user == null) {
            return;
        }

        final User updatedUser = user.withUpdatedStats(pusherStats);
        updateUserJson(updatedUser);
    }
}
