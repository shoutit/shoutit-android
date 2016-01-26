package com.shoutit.app.android;

import android.app.Application;

import com.crashlytics.android.core.CrashlyticsCore;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.AppModule;
import com.shoutit.app.android.dagger.BaseModule;
import com.shoutit.app.android.dagger.DaggerAppComponent;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        initFabric();

        setupGraph();
    }

    private void initFabric() {
        final CrashlyticsCore crashlytics = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();

        Fabric.with(this, crashlytics);
    }

    private void setupGraph() {
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .baseModule(new BaseModule())
                .build();
        component.inject(this);
    }

    public static AppComponent getAppComponent(Application app) {
        return ((App) app).component;
    }
}
