package com.shoutit.app.android;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.observables.NetworkObservableProvider;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.karumi.dexter.Dexter;
import com.pusher.client.Pusher;
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
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.pusher.PusherHelper;
import com.shoutit.app.android.utils.stackcounter.StackCounterManager;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class App extends MultiDexApplication {

    private static final String GCM_TOKEN = "935842257865";

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
    Twilio mTwilio;
    @Inject
    MixPanel mixPanel;
    @Inject
    StackCounterManager mStackCounterManager;
    @Inject
    ProfilesDao profilesDao;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        initFabric();
        initUserVoice();
        logRxJavaErrors();

        setupGraph();

        fetchLocation();
        refreshUser();

        Dexter.initialize(this);

        mixPanel.initMixPanel();
        mixPanel.trackAppOpen();

        initFfmpeg();

        initGcm();

        initPusher();

        initTwilio();

        mStackCounterManager.register(this)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean foreground) {
                        if (userPreferences.isNormalUser()) {
                            final Pusher pusher = mPusherHelper.getPusher();
                            if (pusher != null) {
                                if (foreground && mPusherHelper.shouldConnect()) {
                                    pusher.connect(mPusherHelper.getEventListener());
                                } else if (!foreground) {
                                    pusher.disconnect();
                                }
                            }
                        }
                    }
                });
    }

    private void initTwilio() {
        userPreferences.getTokenObservable()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String userToken) {
                        return userToken != null && !userPreferences.isGuest();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String ignore) {
                        mTwilio.initTwilio();
                    }
                });
    }

    private void refreshUser() {
        if (!userPreferences.isNormalUser()) {
            return;
        }

        profilesDao.updateUser()
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        userPreferences.updateUserJson(user);
                    }
                });
    }

    private void initGcm() {
        AppuniteGcm.initialize(this, GCM_TOKEN)
                .loggingEnabled(!BuildConfig.DEBUG)
                .getPushBundleObservable()
                .subscribe(NotificationHelper.sendNotificationAction(this));
    }

    private void initPusher() {
        userPreferences.getTokenObservable()
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String token) {
                        return token != null && !userPreferences.isGuest();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String token) {
                        final User user = userPreferences.getUser();
                        if (user != null) {
                            initPusher(token, user);
                        }
                    }
                });
    }

    private void initPusher(@Nonnull String token, @Nonnull User user) {
        mPusherHelper.init(token);
        final Pusher pusher = mPusherHelper.getPusher();

        if (mPusherHelper.shouldConnect()) {
            pusher.connect();
            pusher.subscribePresence(PusherHelper.getProfileChannelName(user.getId()));
            mNetworkObservableProvider.networkObservable()
                    .filter(new Func1<NetworkObservableProvider.NetworkStatus, Boolean>() {
                        @Override
                        public Boolean call(NetworkObservableProvider.NetworkStatus networkStatus) {
                            return networkStatus.isNetwork();
                        }
                    })
                    .subscribe(new Action1<NetworkObservableProvider.NetworkStatus>() {
                        @Override
                        public void call(NetworkObservableProvider.NetworkStatus networkStatus) {
                            pusher.connect(mPusherHelper.getEventListener());
                        }
                    });
        }
    }

    private void initFfmpeg() {
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException ignored) {
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
}
