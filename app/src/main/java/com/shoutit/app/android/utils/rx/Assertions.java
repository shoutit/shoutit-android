package com.shoutit.app.android.utils.rx;

import android.os.Looper;

public final class Assertions {
    private Assertions() {
        throw new AssertionError("No instances");
    }

    public static void assertUiThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Observers must subscribe from the main UI thread, but was " + Thread.currentThread());
        }
    }
}