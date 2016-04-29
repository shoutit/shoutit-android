package com.shoutit.app.android.utils;

import android.os.Build;

public class VersionUtils {

    public static boolean isAtLeastLollipop() {
        return Build.VERSION.SDK_INT >= 21;
    }

}