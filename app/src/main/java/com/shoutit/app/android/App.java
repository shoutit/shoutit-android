package com.shoutit.app.android;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;
import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.observables.NetworkObservableProvider;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.User;
import com.shoutit.app.android.constants.UserVoiceConstants;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.AppModule;
import com.shoutit.app.android.dagger.BaseModule;
import com.shoutit.app.android.dagger.DaggerAppComponent;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.AviaryContants;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.stackcounter.StackCounterManager;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.squareup.leakcanary.LeakCanary;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import rx.Scheduler;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class App extends MultiDexApplication implements IAdobeAuthClientCredentials {

    private static final String GCM_TOKEN = "935842257865";
    private static final String TAG = App.class.getCanonicalName();

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
    @Inject
    PusherHelper mPusherHelper;
    @Inject
    NetworkObservableProvider mNetworkObservableProvider;
    @Inject
    MixPanel mixPanel;
    @Inject
    StackCounterManager mStackCounterManager;
    @Inject
    ProfilesDao profilesDao;
    @Inject
    NotificationHelper notificationHelper;
    @Inject
    FacebookHelper facebookHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        initFabric();
        initUserVoice();
        logRxJavaErrors();

        if (BuildConfig.BUILD_TYPE.contains("debug")) {
            LeakCanary.install(this);
        }

        setupGraph();

        fetchLocation();
        refreshUser();

        setUpMixPanel();

        initFfmpeg();

        initGcm();

        setUpPusher();

        facebookHelper.initFacebook();

        AdobeCSDKFoundation.initializeCSDKFoundation(this);
    }

    private void setUpMixPanel() {
        mixPanel.initMixPanel();
        mStackCounterManager.register(this)
                .withLatestFrom(userPreferences.getMixpanelCampaignParamsObservable(), (foreground, params) -> {
                    mixPanel.trackAppOpenOrClose(foreground, params);
                    return null;
                })
                .subscribe();
    }

    private void refreshUser() {
        if (!userPreferences.isNormalUser()) {
            return;
        }

        profilesDao.updateUser()
                .subscribe(user -> {
                    userPreferences.updateUserJson(user);
                });
    }

    private void initGcm() {
        AppuniteGcm.initialize(this, GCM_TOKEN)
                .loggingEnabled(!BuildConfig.DEBUG)
                .getPushBundleObservable()
                .subscribe(notificationHelper.sendNotificationAction(this));
    }

    private void setUpPusher() {
        userPreferences.getTokenObservable()
                .filter(token -> token != null && !userPreferences.isGuest())
                .subscribe(token -> {
                    final User user = userPreferences.getUser();
                    if (user != null) {
                        initPusher(token, user);
                    }
                });

        mStackCounterManager.register(this)
                .subscribe(foreground -> {
                    if (userPreferences.isNormalUser() && mPusherHelper.isInit()) {
                        if (foreground && mPusherHelper.shouldConnect()) {
                            mPusherHelper.connect();
                        } else if (!foreground) {
                            mPusherHelper.disconnect();
                        }
                    }
                });
    }

    private void initPusher(@Nonnull String token, @Nonnull User user) {
        mPusherHelper.init(token);
        if (mPusherHelper.shouldConnect()) {
            mPusherHelper.connect();
            mPusherHelper.subscribeProfileChannel(user.getId());
            mPusherHelper.getUserUpdatedObservable()
                    .subscribe(user1 -> {
                        userPreferences.updateUserJson(user1);
                    });

            mPusherHelper.getStatsObservable()
                    .subscribe(stats -> {
                        userPreferences.updateStats(stats);
                    });

            mNetworkObservableProvider.networkObservable()
                    .filter(NetworkObservableProvider.NetworkStatus::isNetwork)
                    .subscribe(networkStatus -> {
                        mPusherHelper.connect();
                    });
        }
    }

    private void initFfmpeg() {
        try {
            final FFmpeg ffmpeg = FFmpeg.getInstance(this);
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException ignored) {
            LogHelper.logThrowableAndCrashlytics(TAG, "ffpmpeg init", ignored);
        }
    }

    private void initUserVoice() {
        final Config config = new Config(UserVoiceConstants.USER_VOICE_WEBSITE);
        config.setShowForum(false);
        config.setTopicId(UserVoiceConstants.USER_VOICE_TOPIC_ID);
        config.setForumId(UserVoiceConstants.USER_VOICE_FORUM_ID);
        UserVoice.init(config, this);
    }

    private void initFabric() {
        if (BuildConfig.enableCrashlytics) {
            Fabric.with(this, new CrashlyticsCore.Builder().build(), new Crashlytics());
        }
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

    @Override
    public String getClientID() {
        return AviaryContants.CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return AviaryContants.CREATIVE_SDK_CLIENT_SECRET;
    }

    @Override
    public String[] getAdditionalScopesList() {
        return new String[0];
    }

    @Override
    public String getRedirectURI() {
        return null;
    }
}
