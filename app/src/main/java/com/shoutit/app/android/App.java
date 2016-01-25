package com.shoutit.app.android;

import android.app.Application;

import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initFabric();
    }

    private void initFabric() {
        final CrashlyticsCore crashlytics = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();

        Fabric.with(this, crashlytics);
    }
}
