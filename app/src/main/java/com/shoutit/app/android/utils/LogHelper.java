package com.shoutit.app.android.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class LogHelper {

    public static void logThrowable(String tag, String msg, Throwable throwable) {
        Log.e(tag, msg, throwable);
        final Throwable cause = throwable.getCause();
        if (cause != null) {
            logThrowable(tag, msg, cause);
        }
    }

    public static void logThrowableAndCrashlytics(String tag, String msg, Throwable t) {
        Log.e(tag, msg, t);
        Crashlytics.logException(t);
    }
}
