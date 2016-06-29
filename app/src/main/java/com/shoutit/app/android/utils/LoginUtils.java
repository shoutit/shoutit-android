package com.shoutit.app.android.utils;

import android.support.annotation.Nullable;

public class LoginUtils {

    public static boolean isPasswordCorrect(@Nullable String password) {
        return password != null && password.length() >= 6 && password.length() <= 20;
    }



}
