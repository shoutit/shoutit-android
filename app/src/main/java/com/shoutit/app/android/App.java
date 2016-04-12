package com.shoutit.app.android;

import android.app.Application;
import android.support.multidex.MultiDex;

import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
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
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.PusherHelper;
import com.shoutit.app.android.view.videoconversation.VideoConversationPresenter;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;


public class App extends VideoConversationsApplication {

    private static final String TAG = App.class.getSimpleName();

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
    VideoConversationPresenter presenter;


    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        initFabric();
        initUserVoice();
        logRxJavaErrors();

        setupGraph();

        fetchLocation();

        Dexter.initialize(this);

        initFfmpeg();

        initGcm();

        initPusher();

        if(userPreferences.isNormalUser()) {
            InitializeVideoConversations();
        }
    }

    private void initGcm() {
        AppuniteGcm.initialize(this, GCM_TOKEN)
                .loggingEnabled(!BuildConfig.DEBUG)
                .getPushBundleObservable()
                .subscribe(NotificationHelper.sendNotificationAction(this));
    }

    private void initPusher() {
        Observable.zip(userPreferences.getTokenObservable().filter(Functions1.isNotNull()),
                userPreferences.getUserObservable().filter(Functions1.isNotNull()),
                new Func2<String, User, BothParams<String, User>>() {
                    @Override
                    public BothParams<String, User> call(String token, User user) {
                        return new BothParams<>(token, user);
                    }
                })
                .first()
                .subscribe(new Action1<BothParams<String, User>>() {
                    @Override
                    public void call(BothParams<String, User> tokenAndUser) {
                        initPusher(tokenAndUser.param1(), tokenAndUser.param2());
                    }
                });
    }

    private void initPusher(@Nonnull String token, @Nonnull User user) {
        mPusherHelper.init(token);
        final Pusher pusher = mPusherHelper.getPusher();
        pusher.connect();
        pusher.subscribePresence(String.format("presence-u-%1$s", user.getId()));
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
        } catch (FFmpegNotSupportedException e) {
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
        if (BuildConfig.enableCrashlytics == true) {
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
