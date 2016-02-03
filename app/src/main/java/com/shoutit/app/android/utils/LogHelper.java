package com.shoutit.app.android.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class LogHelper {

    public static void logThrowable(String tag, String msg, Throwable t) {
        // TODO filter out no connection exceptions
        Log.e(tag, msg, t);
        final Throwable cause = t.getCause();
        if (cause != null) {
            logThrowable(tag, msg, cause);
        }
    }

    public static void logThrowableAndCrashlytics(String tag, String msg, Throwable t) {
        Log.e(tag, msg, t);
        Crashlytics.logException(t);
    }
}
