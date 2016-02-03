package com.shoutit.app.android.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class TextHelper {

    public static String emptyToNull(@Nullable String text) {
        return TextUtils.isEmpty(text) ? null : text;
    }
}
