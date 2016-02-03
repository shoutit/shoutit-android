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
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.dagger.ForApplication;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public class UserPreferences {

    private static final String AUTH_TOKEN = "token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER = "user";

    private final PublishSubject<Object> userRefreshSubject = PublishSubject.create();

    @SuppressLint("CommitPrefEdits")
    private final SharedPreferences mPreferences;
    @Nonnull
    private final Gson gson;

    @Inject
    public UserPreferences(@ForApplication Context context, @Nonnull Gson gson) {
        this.gson = gson;
        mPreferences = context.getSharedPreferences("prefs", 0);
    }

    @SuppressLint("CommitPrefEdits")
    public void setLoggedIn(@NonNull String authToken, @NonNull String refreshToken) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(AUTH_TOKEN, authToken);
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.commit();
    }

    public Optional<String> getAuthToken() {
        return Optional.fromNullable(mPreferences.getString(AUTH_TOKEN, null));
    }

    // TODO
    @Nullable
    public String getUserCountryCode() {
        return "GE";
    }

    // TODO
    @Nullable
    public String getUserCity() {
        return "Dubaj";
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

    @Nullable
    public User getUser() {
        final String userJson = mPreferences.getString(KEY_USER, null);
        return gson.fromJson(userJson, User.class);
    }

    @Nonnull
    public Observable<User> userObservable() {
        return Observable
                .fromCallable(new Callable<User>() {
                    @Override
                    public User call() throws Exception {
                        return getUser();
                    }
                })
                .filter(Functions1.isNotNull())
                .compose(MoreOperators.<User>refresh(userRefreshSubject));
    }

    private void refreshUser() {
        userRefreshSubject.onNext(null);
    }
}
