package com.shoutit.app.android.utils;

import android.os.Build;

public class VersionUtils {

    public static boolean isAtLeastL() {
        return Build.VERSION.SDK_INT >= 21;
    }

}