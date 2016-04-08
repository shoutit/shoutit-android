package com.appunite.appunitegcm;

import android.support.annotation.NonNull;

public class GcmPushDataWithServerKey {

    @NonNull
    private final String gcmServerKey;
    @NonNull
    private final String gcmPushData;

    public GcmPushDataWithServerKey(@NonNull String gcmServerKey,
                                    @NonNull String gcmPushData) {
        this.gcmServerKey = gcmServerKey;
        this.gcmPushData = gcmPushData;
    }

    @NonNull
    public String getGcmServerKey() {
        return gcmServerKey;
    }

    @NonNull
    public String getGcmPushData() {
        return gcmPushData;
    }
}
