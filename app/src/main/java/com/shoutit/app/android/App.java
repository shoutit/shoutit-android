package com.shoutit.app.android;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.appunite.rx.dagger.NetworkScheduler;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.karumi.dexter.Dexter;
import com.shoutit.app.android.api.ApiService;
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
import rx.Scheduler;
import rx.functions.Action1;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

public class App extends MultiDexApplication {

    private static final String VC = "TWILIO";
    private static final String TAG = App.class.getSimpleName();

    private AppComponent component;
    private String apiKey;

    private TwilioAccessManager accessManager;
    private ConversationsClient conversationsClient;
    private IncomingInvite invite;
    private OutgoingInvite outgoingInvite;

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
    PusherHelper pusher;
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

        initPusher();

        presenter.getTwilioRequirementObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String apiKey) {
//                        initializeVideoCalls(apiKey);
                        Log.d("TWILIO", "MY API KEY: " + apiKey);
                    }
                });
    }

    private void initPusher() {
        if (userPreferences.isUserLoggedIn()) {
            pusher.init(userPreferences.getAuthToken().get());
            pusher.getPusher().connect();
        } else {
            userPreferences.getTokenObservable()
                    .first()
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String token) {
                            pusher.init(token);
                            pusher.getPusher().connect();
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

    /** Initialize Video Conversations **/
    private void initializeVideoCalls(@Nonnull final String apiKey){
        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        if(!TwilioConversations.isInitialized()) {
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

    /** Check Token Status **/
    private TwilioAccessManagerListener accessManagerListener() {
        return new TwilioAccessManagerListener() {
            @Override
            public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                Log.d(VC,"Token Expired");

            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
                Log.d(VC, "Token Updated");

            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
                Log.d(VC, "Error");
            }
        };
    }

    /** Conversation Status **/
    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                Log.d(VC, "Listen for Conversations");
            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                Log.d(VC, "Stop listening for Conversations");
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException e) {
                Log.d(VC, "Failed to listening for Conversations");
            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                Log.d(VC, "Incoming call");
                invite = incomingInvite;
                Intent intent = new Intent(getApplicationContext(), DialogCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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

    public ConversationsClient getConversationsClient(){
        return conversationsClient;
    }

    public void setConversationsClient(ConversationsClient conversationsClient) {
        this.conversationsClient = conversationsClient;
        conversationsClient.listen();
    }

}
