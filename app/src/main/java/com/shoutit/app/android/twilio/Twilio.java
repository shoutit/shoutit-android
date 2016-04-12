package com.shoutit.app.android.twilio;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.api.model.CallerProfile;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.dao.VideoCallsDao;
import com.shoutit.app.android.view.videoconversation.DialogCallActivity;
import com.twilio.common.TwilioAccessManager;
import com.twilio.common.TwilioAccessManagerFactory;
import com.twilio.common.TwilioAccessManagerListener;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

@Singleton
public class Twilio {

    private static final String TAG = Twilio.class.getCanonicalName();
    private final Context mContext;
    private TwilioAccessManager accessManager;
    private ConversationsClient conversationsClient;
    private IncomingInvite invite;

    @Nonnull
    private Observable<String> twilioRequirementObservable;
    @Nonnull
    private Observable<String> callerNameObservable;
    @Nonnull
    private Observable<Throwable> errorObservable;

    @Nonnull
    private BehaviorSubject<String> callerIdentitySubject = BehaviorSubject.create();

    @Inject
    public Twilio(@ForApplication Context context,
                  @Nonnull final VideoCallsDao videoCallsDao,
                  @Nonnull final UsersIdentityDao usersIdentityDao,
                  @Nonnull @UiScheduler final Scheduler uiScheduler) {
        mContext = context;

        final Observable<ResponseOrError<TwilioResponse>> twilioResponse = videoCallsDao.getVideoCallsObservable()
                .compose(ObservableExtensions.<ResponseOrError<TwilioResponse>>behaviorRefCount());

        Observable<ResponseOrError<CallerProfile>> callerProfileResponse = callerIdentitySubject
                .flatMap(new Func1<String, Observable<ResponseOrError<CallerProfile>>>() {
                    @Override
                    public Observable<ResponseOrError<CallerProfile>> call(String callerName) {
                        return usersIdentityDao.getUserByIdentityObservable(callerName);
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<CallerProfile>>behaviorRefCount());

        twilioRequirementObservable = twilioResponse
                .compose(ResponseOrError.<TwilioResponse>onlySuccess())
                .map(new Func1<TwilioResponse, String>() {
                    @Override
                    public String call(TwilioResponse twilioResponse) {
                        return twilioResponse.getToken();
                    }
                }).observeOn(uiScheduler)
                .filter(Functions1.isNotNull());


        callerNameObservable = callerProfileResponse
                .compose(ResponseOrError.<CallerProfile>onlySuccess())
                .filter(Functions1.isNotNull())
                .map(new Func1<CallerProfile, String>() {
                    @Override
                    public String call(CallerProfile callerProfile) {
                        return callerProfile.getName();
                    }
                }).observeOn(uiScheduler);

        /** Errors **/
        errorObservable = ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(twilioResponse),
                ResponseOrError.transform(callerProfileResponse)))
                .filter(Functions1.isNotNull()).doOnNext(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                })
                .observeOn(uiScheduler);
    }

    public void init() {
        twilioRequirementObservable
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String apiKey) {
                        initializeVideoCalls(apiKey);
                    }
                });

        errorObservable
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(mContext, "Failed to fetch data: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeVideoCalls(@Nonnull final String apiKey) {
        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        if (!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(mContext, new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    accessManager = TwilioAccessManagerFactory.createAccessManager(apiKey, accessManagerListener());
                    conversationsClient = TwilioConversations.createConversationsClient(accessManager, conversationsClientListener());
                    conversationsClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
                    conversationsClient.listen();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(mContext, "Failed to initialize the Twilio Conversations SDK", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private TwilioAccessManagerListener accessManagerListener() {
        return new TwilioAccessManagerListener() {
            @Override
            public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
                Log.d(TAG, "accessManagerListener : Token Expired");
            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
                Log.d(TAG, "accessManagerListener : Token Updated");
            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
                Log.d(TAG, "accessManagerListener : Error on Token");
            }
        };
    }

    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {
                Log.d("TWILIO", "LISTENING ** ** **");
            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                conversationsClient.listen();
                Log.d(TAG, "Stop listening for Conversations");
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException e) {
                if (e != null) {
                    twilioRequirementObservable
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String apiKey) {
                                    initializeVideoCalls(apiKey);
                                }
                            });
                }
                conversationsClient.listen();
                Log.d(TAG, "Failed to listening for Conversations");
            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                invite = incomingInvite;
                String caller = String.valueOf(incomingInvite.getParticipants());

                callerIdentitySubject.onNext(caller.substring(1, caller.length() - 1));
                callerNameObservable
                        .take(1)
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String callerName) {
                                Intent intent = DialogCallActivity.newIntent(callerName, mContext);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        });
            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                Log.d(TAG, "Incoming call canceled");
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

}
