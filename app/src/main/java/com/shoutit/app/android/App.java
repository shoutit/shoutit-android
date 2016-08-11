package com.shoutit.app.android;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.observables.NetworkObservableProvider;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.newrelic.agent.android.NewRelic;
import com.newrelic.agent.android.logging.AgentLog;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.constants.UserVoiceConstants;
import com.shoutit.app.android.dagger.AppComponent;
import com.shoutit.app.android.dagger.AppModule;
import com.shoutit.app.android.dagger.BaseModule;
import com.shoutit.app.android.dagger.DaggerAppComponent;
import com.shoutit.app.android.dao.ProfilesDao;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.AviaryContants;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.ProcessUtils;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.pusher.PusherHelperHolder;
import com.shoutit.app.android.utils.stackcounter.StackCounterManager;
import com.squareup.leakcanary.LeakCanary;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func2;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class App extends MultiDexApplication implements IAviaryClientCredentials {

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
    Provider<PusherHelper> mPusherHelperProvider;
    @Inject
    NetworkObservableProvider mNetworkObservableProvider;
    @Inject
    Twilio mTwilio;
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

    @Inject
    PusherHelperHolder mCurrentUserPusherHelper;
    @Inject
    @Named("user")
    PusherHelperHolder mUserPusherHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!ProcessUtils.isInMainProcess(this)) {
            return;
        }

        MultiDex.install(this);

        initFabric();

        initNewRelic();

        initUserVoice();

        logRxJavaErrors();

        if (BuildConfig.BUILD_TYPE.contains("debug")) {
            LeakCanary.install(this);
        }

        setupGraph();

        fetchLocation();

        setUpMixPanel();

        initFfmpeg();

        initGcm();

        setUpPusher();

        initTwilio();

        facebookHelper.initFacebook();

        AdobeCSDKFoundation.initializeCSDKFoundation(this);
    }

    private void initNewRelic() {
        NewRelic.withApplicationToken("AA52f20a8d2726a5f9d33f69160e55aa21aa557c54")
                .withLogLevel(BuildConfig.DEBUG ? AgentLog.INFO : AgentLog.ERROR)
                .withLoggingEnabled(BuildConfig.DEBUG)
                .withCrashReportingEnabled(false)
                .start(this);
    }

    private void setUpMixPanel() {
        mixPanel.initMixPanel();
        mStackCounterManager.register(this)
                .subscribe(foreground -> {
                    mixPanel.trackAppOpenOrClose(foreground);
                });
    }

    private void initTwilio() {
        Observable.combineLatest(userPreferences.getTokenObservable().filter(userToken -> userToken != null && !userPreferences.isGuest()),
                userPreferences.getTwilioTokenObservable().filter(Functions1.isNull()).startWith((String) null),
                (Func2<String, String, Object>) (s, s2) -> s)
                .subscribe(ignore -> {
                    mTwilio.initTwilio();
                });
    }

    private void initGcm() {
        AppuniteGcm.initialize(this, GCM_TOKEN)
                .loggingEnabled(!BuildConfig.DEBUG)
                .getPushBundleObservable()
                .subscribe(notificationHelper.sendNotificationAction());
    }

    private void setUpPusher() {
        Observable.combineLatest(
                userPreferences.getTokenObservable().filter(token -> token != null && !userPreferences.isGuest()),
                userPreferences.getPageIdObservable(),
                (s, s2) -> s)
                .subscribe(token -> {
                    final BaseProfile baseProfile = userPreferences.getUserOrPage();
                    if (baseProfile != null) {
                        initPusher(token, baseProfile);
                    }
                });

        userPreferences.getTokenObservable()
                .switchMap(s -> userPreferences.getPageIdObservable())
                .subscribe(pageId -> {
                    if (pageId != null) {
                        final PusherHelper pusherHelper = mUserPusherHelper.newInstance();
                        pusherHelper.init(userPreferences.getAuthToken().get(), userPreferences.getUser());
                        pusherHelper.connect();
                        pusherHelper.subscribeProfileChannel();
                        pusherHelper.getStatsObservable()
                                .subscribe(stats -> {
                                    userPreferences.updateUserStats(stats);
                                });
                    } else {
                        final PusherHelper pusherHelper = mUserPusherHelper.getPusherHelper();
                        if (pusherHelper != null) {
                            pusherHelper.unsubscribeProfileChannel();
                            pusherHelper.disconnect();
                        }
                    }
                });

        mStackCounterManager.register(this)
                .subscribe(foreground -> {
                    final PusherHelper pusherHelper = mCurrentUserPusherHelper.getPusherHelper();
                    if (userPreferences.isNormalUser() && pusherHelper != null && pusherHelper.isInit()) {
                        if (foreground && pusherHelper.shouldConnect()) {
                            pusherHelper.connect();
                        } else if (!foreground) {
                            pusherHelper.disconnect();
                        }
                    }

                    final PusherHelper userPusherHelper = mUserPusherHelper.getPusherHelper();
                    if (userPreferences.isNormalUser() && userPusherHelper != null && userPusherHelper.isInit()) {
                        if (foreground && userPusherHelper.shouldConnect()) {
                            userPusherHelper.connect();
                        } else if (!foreground) {
                            userPusherHelper.disconnect();
                        }
                    }
                });
    }

    private void initPusher(@Nonnull String token, @Nonnull BaseProfile user) {
        final PusherHelper pusherHelper = mCurrentUserPusherHelper.getPusherHelper();
        if (pusherHelper != null) {
            pusherHelper.disconnect();
            pusherHelper.unsubscribeProfileChannel();
        }

        final PusherHelper newPusherHelper = mCurrentUserPusherHelper.newInstance();
        newPusherHelper.init(token, user);
        if (newPusherHelper.shouldConnect()) {
            newPusherHelper.connect();
            newPusherHelper.subscribeProfileChannel();
            newPusherHelper.getUserUpdatedObservable()
                    .subscribe(user1 -> {
                        userPreferences.setUserOrPage(user1);
                    });

            newPusherHelper.getStatsObservable()
                    .subscribe(stats -> {
                        userPreferences.updateStats(stats);
                    });

            mNetworkObservableProvider.networkObservable()
                    .filter(NetworkObservableProvider.NetworkStatus::isNetwork)
                    .subscribe(networkStatus -> {
                        newPusherHelper.connect();
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
    public String getBillingKey() {
        return null;
    }
}
