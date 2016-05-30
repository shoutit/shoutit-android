package com.shoutit.app.android.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.shoutit.app.android.BuildConfig;

import javax.annotation.Nonnull;

public class LogHelper {

    public static void logThrowable(String tag, String msg, Throwable t) {
        try {
            Log.e(tag, msg, t);
            final Throwable cause = t.getCause();
            if (cause != null) {
                logThrowable(tag, msg, cause);
            }
        } catch (Throwable th) {
            Log.e(tag, msg, th);
        }
    }

    public static void logThrowableAndCrashlytics(String tag, String msg, Throwable t) {
        if (BuildConfig.enableCrashlytics == true) {
            Crashlytics.logException(t);
        } else {
            Log.e(tag, msg, t);
        }
    }

    public static void logIfDebug(@Nonnull String tag, @Nonnull String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }
}
