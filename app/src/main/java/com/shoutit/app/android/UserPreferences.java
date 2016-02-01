package com.shoutit.app.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.shoutit.app.android.dagger.ForApplication;

import javax.inject.Inject;

public class UserPreferences {

    private static final String AUTH_TOKEN = "token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @SuppressLint("CommitPrefEdits")
    private final SharedPreferences mPreferences;

    @SuppressLint("CommitPrefEdits")
    public void setLoggedIn(@NonNull String authToken, @NonNull String refreshToken) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(AUTH_TOKEN, authToken);
        editor.putString(REFRESH_TOKEN, refreshToken);
        editor.commit();
    }

    @Inject
    public UserPreferences(@ForApplication Context context) {
        mPreferences = context.getSharedPreferences("prefs", 0);
    }

    public Optional<String> getAuthToken() {
        return Optional.fromNullable(mPreferences.getString(AUTH_TOKEN, null));
    }
}
