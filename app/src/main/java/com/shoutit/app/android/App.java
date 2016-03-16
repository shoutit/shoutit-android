package com.shoutit.app.android;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.appunite.rx.dagger.NetworkScheduler;
import com.crashlytics.android.core.CrashlyticsCore;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.constants.UserVoiceConstants;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.AppModule;
import com.shoutit.app.android.dagger.BaseModule;
import com.shoutit.app.android.dagger.DaggerAppComponent;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.utils.LogHelper;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import rx.Scheduler;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

public class App extends MultiDexApplication {
    private static final String TAG = App.class.getSimpleName();

    private AppComponent component;

    @Inject
    ApiService apiService;
    @Inject
    @NetworkScheduler
    Scheduler networkScheduler;
    @Inject
    UserPreferences userPreferences;
    @Inject
    LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        initFabric();
        initUserVoice();
        logRxJavaErrors();

        setupGraph();

        fetchLocation();
    }

    private void initUserVoice() {
        final Config config = new Config(UserVoiceConstants.USER_VOICE_WEBSITE);
        config.setShowForum(false);
        config.setTopicId(UserVoiceConstants.USER_VOICE_TOPIC_ID);
        config.setForumId(UserVoiceConstants.USER_VOICE_FORUM_ID);
        UserVoice.init(config, this);
    }

    private void initFabric() {
        final CrashlyticsCore crashlytics = new CrashlyticsCore.Builder()
                .disabled("debug".equals(BuildConfig.BUILD_TYPE))
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


    private void logRxJavaErrors() {
        if (BuildConfig.DEBUG) {
            RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
                @Override
                public void handleError(Throwable e) {
                    LogHelper.logThrowable("rxjava error handler", "error", e);
                    super.handleError(e);
                }
            });
        }
    }

    public static AppComponent getAppComponent(Application app) {
        return ((App) app).component;
    }

    private void fetchLocation() {
        locationManager.updateUserLocationObservable()
                .subscribe();
    }
}
