package com.shoutit.app.android.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.common.base.Strings;


public class Validators {

    public static boolean isEmailValid(@Nullable String email) {
        return !Strings.isNullOrEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
