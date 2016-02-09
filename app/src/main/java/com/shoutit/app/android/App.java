package com.shoutit.app.android;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.appunite.rx.dagger.NetworkScheduler;
import com.crashlytics.android.core.CrashlyticsCore;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;
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
import rx.functions.Action1;
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

        fetchUser();
        updateLocationIfNeeded();
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

    private void fetchUser() {
        if (!userPreferences.isUserLoggedIn()) {
            return;
        }

        apiService.getMyUser()
                .subscribeOn(networkScheduler)
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        userPreferences.saveUserAsJson(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogHelper.logThrowable(TAG, "Cannot fetch user", throwable);
                    }
                });
    }

    private void updateLocationIfNeeded() {
        locationManager.updateUserLocationObservable()
                .subscribe();
    }
}
