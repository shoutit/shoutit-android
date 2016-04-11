package com.shoutit.app.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.appunite.appunitegcm.AppuniteGcm;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.appunite.rx.observables.NetworkObservableProvider;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.karumi.dexter.Dexter;
import com.pusher.client.Pusher;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
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
import com.shoutit.app.android.view.videoconversation.DialogCallActivity;
import com.shoutit.app.android.view.videoconversation.VideoConversationPresenter;
import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsException;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

public class App extends MultiDexApplication {

    private static final String VC = "APP_TWILIO";
    private static final String TAG = App.class.getSimpleName();

    private static final String GCM_TOKEN = "935842257865";

    private AppComponent component;
    private String apiKey;

    private OutgoingInvite outgoingInvite;

    private TwilioAccessManager accessManager;
    private ConversationsClient conversationsClient;
    private IncomingInvite invite;
    private ConnectionEventListener mEventListener = new ConnectionEventListener() {
        @Override
        public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
            Log.i(TAG, connectionStateChange.getCurrentState().name());
        }

        @Override
        public void onError(String s, String s1, Exception e) {
            Log.e(TAG, "pusher message", e);
        }
    };

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
    @Inject
    NetworkObservableProvider mNetworkObservableProvider;

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

        presenter.getTwilioRequirementObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String apiKey) {
                        initializeVideoCalls(apiKey);
                    }
                });

        presenter.getErrorObservable()
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getApplicationContext(), "Failed to fetch data: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
        pusher.connect(mEventListener);
        pusher.subscribePresence(String.format("presence-u-%1$s", user.getId()));

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
                        final ConnectionState state = pusher.getConnection().getState();
                        if (state != ConnectionState.CONNECTED && state != ConnectionState.CONNECTING) {
                            pusher.connect(mEventListener);
                        }
                    }
                });
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

    /**
     * Initialize Video Conversations
     **/
    private void initializeVideoCalls(@Nonnull final String apiKey) {
        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        if (!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(this, new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    accessManager = TwilioAccessManagerFactory.createAccessManager(apiKey, accessManagerListener());
                    conversationsClient = TwilioConversations.createConversationsClient(accessManager, conversationsClientListener());
                    conversationsClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
                    conversationsClient.listen();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to initialize the Twilio Conversations SDK", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Check Token Status
     **/
    private TwilioAccessManagerListener accessManagerListener() {
        return new TwilioAccessManagerListener() {
            @Override
            public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                Log.d(VC, "accessManagerListener : Token Expired");
            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
                Log.d(VC, "accessManagerListener : Token Updated");
            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
                Log.d(VC, "accessManagerListener : Error on Token");
            }
        };
    }

    /**
     * Conversation Status
     **/
    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                Log.d("TWILIO", "LISTENING ** ** **");
            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                conversationsClient.listen();
                Log.d(VC, "Stop listening for Conversations");
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException e) {
                if (e != null) {
                    presenter.getTwilioRequirementObservable()
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String apiKey) {
                                    initializeVideoCalls(apiKey);
                                }
                            });
                }
                conversationsClient.listen();
                Log.d(VC, "Failed to listening for Conversations");
            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                invite = incomingInvite;
                String caller = String.valueOf(incomingInvite.getParticipants());

                presenter.setCallerIdentity(caller.substring(1, caller.length() - 1));
                presenter.getCallerNameObservable()
                        .take(1)
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String callerName) {
                                Intent intent = DialogCallActivity.newIntent(callerName, getApplicationContext());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                Log.d(VC, "Incoming call canceled");
                conversationsClient.listen();
            }
        };
    }

    @Nullable
    public IncomingInvite getInvite() {
        return invite;
    }

    public ConversationsClient getConversationsClient() {
        return conversationsClient;
    }

    public void setConversationsClient(ConversationsClient conversationsClient) {
        this.conversationsClient = conversationsClient;
        conversationsClient.listen();
    }

}
