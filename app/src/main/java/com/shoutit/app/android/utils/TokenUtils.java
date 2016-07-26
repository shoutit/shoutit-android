package com.shoutit.app.android.utils;

import android.support.annotation.NonNull;

import com.shoutit.app.android.UserPreferences;

public class TokenUtils {

    private static final String TAG = TokenUtils.class.getSimpleName();

    public static boolean isTokenExpired(@NonNull UserPreferences userPreferences) {
        final long tokenSaveDate = userPreferences.getTokenSaveDate();
        final long deviceDate = System.currentTimeMillis();
        final long tokenExpiresIn = userPreferences.getTokenExpiresInAsMillis();

        final long dateWhenTokenExpires = tokenSaveDate + tokenExpiresIn;

        boolean isTokenExpired = deviceDate >= dateWhenTokenExpires;
        if (isTokenExpired) {
            LogHelper.logIfDebug(TAG, "Token expired by " + (deviceDate - dateWhenTokenExpires) + " millis");
        }

        return isTokenExpired;
    }
}
