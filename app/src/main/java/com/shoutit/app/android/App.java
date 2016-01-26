package com.shoutit.app.android;

import android.app.Application;

import com.crashlytics.android.core.CrashlyticsCore;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.AppModule;
import com.shoutit.app.android.dagger.BaseModule;
import com.shoutit.app.android.dagger.DaggerAppComponent;
import com.shoutit.app.android.data.Constants;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        initFabric();
        initUserVoice();

        setupGraph();
    }

    private void initUserVoice() {
        final Config config = new Config(Constants.USER_VOICE_WEBSITE);
        config.setShowForum(false);
        config.setTopicId(Constants.USER_VOICE_TOPIC_ID);
        config.setForumId(Constants.USER_VOICE_FORUM_ID);
        UserVoice.init(config, this);
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
