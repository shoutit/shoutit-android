package com.shoutit.app.android.twilio;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.ResponseOrError;
import com.appunite.rx.dagger.NetworkScheduler;
import com.appunite.rx.dagger.UiScheduler;
import com.appunite.rx.functions.BothParams;
import com.appunite.rx.functions.Functions1;
import com.google.common.collect.ImmutableList;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.CallerProfile;
import com.shoutit.app.android.api.model.TwilioResponse;
import com.shoutit.app.android.api.model.UserIdentity;
import com.shoutit.app.android.api.model.VideoCallRequest;
import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.dao.UsersIdentityDao;
import com.shoutit.app.android.dao.VideoCallsDao;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.view.videoconversation.DialogCallActivity;
import com.twilio.common.AccessManager;
import com.twilio.conversations.AudioOutput;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.InviteStatus;
import com.twilio.conversations.LogLevel;
import com.twilio.conversations.TwilioConversationsClient;
import com.twilio.conversations.TwilioConversationsException;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

@Singleton
public class Twilio {

    private static final String TAG = Twilio.class.getCanonicalName();
    private static final int TOKEN_ERROR_MAX_RETRIES = 3;
    private static final int TOKEN_EXPIRED_MAX_RETRIES = 3;
    private static final int LISTEN_FAIL_MAX_RETRIES = 3;

    public static final int ERROR_PARTICIPANT_UNAVAILABLE = 106;
    public static final int ERROR_PARTICIPANT_REJECTED_CALL = 107;
    public static final int ERROR_PARTICIPANT_IS_BUSY = 108;

    private final Context mContext;
    @Nonnull
    private final UserPreferences userPreferences;
    private TwilioConversationsClient conversationsClient;
    private IncomingInvite currentInvite;
    private boolean isDuringCall;
    private int tokenErrorRetries;
    private int tokeExpiredRetries;
    private int listenFailRetries;

    private final Observable<String> successTwilioTokenRequestObservable;
    private final Observable<String> successCalledPersonIdentity;
    private final Observable<Throwable> errorCalledPersonIdentity;

    @Nonnull
    private final PublishSubject<BothParams<String, String>> profileRefreshSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> initCalledPersonIdentityRequestSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<String> rejectCallSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Object> initTwilioSubject = PublishSubject.create();
    @Nonnull
    private final PublishSubject<Throwable> errorSubject = PublishSubject.create();

    @Inject
    public Twilio(@ForApplication Context context,
                  @Nonnull final VideoCallsDao videoCallsDao,
                  @Nonnull final UsersIdentityDao usersIdentityDao,
                  @Nonnull final ApiService apiService,
                  @Nonnull final UserPreferences userPreferences,
                  @Nonnull @NetworkScheduler final Scheduler networkScheduler,
                  @Nonnull @UiScheduler final Scheduler uiScheduler) {
        mContext = context;
        this.userPreferences = userPreferences;

        successTwilioTokenRequestObservable = videoCallsDao.getVideoCallsObservable()
                .map(new Func1<ResponseOrError<TwilioResponse>, String>() {
                    @Override
                    public String call(ResponseOrError<TwilioResponse> twilioResponse) {
                        if (twilioResponse.isData()) {
                            return twilioResponse.data().getToken();
                        } else {
                            errorSubject.onNext(twilioResponse.error());
                            return null;
                        }
                    }
                })
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler);

        initTwilioSubject
                .filter(o -> !userPreferences.isGuest())
                .switchMap(ignore -> Observable.just(userPreferences.getTwilioToken()))
                .switchMap(twilioToken -> {
                    if (TextUtils.isEmpty(twilioToken)) {
                        return successTwilioTokenRequestObservable;
                    } else {
                        return Observable.just(twilioToken);
                    }
                })
                .subscribe(this::initializeTwilio);

        final Observable<ResponseOrError<BothParams<CallerProfile, String>>> callerProfileResponse = profileRefreshSubject
                .switchMap(new Func1<BothParams<String, String>, Observable<ResponseOrError<BothParams<CallerProfile, String>>>>() {
                    @Override
                    public Observable<ResponseOrError<BothParams<CallerProfile, String>>> call(BothParams<String, String> conversationIdWithIdentity) {
                        return usersIdentityDao.getUserByIdentityObservable(conversationIdWithIdentity.param2())
                                .compose(ResponseOrError.map(new Func1<CallerProfile, BothParams<CallerProfile, String>>() {
                                    @Override
                                    public BothParams<CallerProfile, String> call(CallerProfile callerProfile) {
                                        return new BothParams<>(callerProfile, conversationIdWithIdentity.param1());
                                    }
                                }));
                    }
                })
                .compose(ObservableExtensions.<ResponseOrError<BothParams<CallerProfile, String>>>behaviorRefCount());


        callerProfileResponse
                .throttleFirst(4, TimeUnit.SECONDS) // Workaround for Twilio mutiple invites
                .compose(ResponseOrError.onlySuccess())
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler)
                .subscribe(new Action1<BothParams<CallerProfile, String>>() {
                    @Override
                    public void call(BothParams<CallerProfile, String> callerProfileWithConversationId) {
                        final CallerProfile callerProfile = callerProfileWithConversationId.param1();
                        final String conversationId = callerProfileWithConversationId.param2();

                        final Intent intent = DialogCallActivity.newIntent(
                                callerProfile.getName(), callerProfile.getImage(), conversationId, mContext);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });

        rejectCallSubject
                .filter(Functions1.isNotNull())
                .switchMap(calledIdentity -> apiService.videoCall(new VideoCallRequest(calledIdentity, true))
                        .subscribeOn(networkScheduler)
                        .compose(ResponseOrError.<ResponseBody>toResponseOrErrorObservable())
                        .compose(ResponseOrError.<ResponseBody>onlyError()))
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogHelper.logThrowable(TAG, "Cannot reject call", throwable);
                    }
                });

        final Observable<ResponseOrError<UserIdentity>> calledPersonIdentityResponse = initCalledPersonIdentityRequestSubject
                .switchMap(username -> usersIdentityDao.getUserIdentityObservable(username)
                        .observeOn(uiScheduler))
                .compose(ObservableExtensions.<ResponseOrError<UserIdentity>>behaviorRefCount());

        successCalledPersonIdentity = calledPersonIdentityResponse
                .compose(ResponseOrError.<UserIdentity>onlySuccess())
                .map(UserIdentity::getIdentity);

        errorCalledPersonIdentity = calledPersonIdentityResponse
                .compose(ResponseOrError.<UserIdentity>onlyError());

        /** Errors **/
        ResponseOrError.combineErrorsObservable(ImmutableList.of(
                ResponseOrError.transform(callerProfileResponse)))
                .mergeWith(errorSubject)
                .filter(Functions1.isNotNull())
                .observeOn(uiScheduler)
                .subscribe(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogHelper.logThrowableAndCrashlytics(TAG, "Twilio error: ", throwable);
                    }
                });
    }

    public void initTwilio() {
        unregisterTwillio();
        initTwilioSubject.onNext(null);
    }

    private void initializeTwilio(@Nonnull final String accessToken) {
        TwilioConversationsClient.setLogLevel(LogLevel.DEBUG);

        if (!TwilioConversationsClient.isInitialized()) {
            TwilioConversationsClient.initialize(mContext);
            AccessManager accessManager = AccessManager.create(mContext, accessToken, accessManagerListener());
            conversationsClient = TwilioConversationsClient.create(accessManager, conversationsClientListener());
            conversationsClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
            conversationsClient.listen();
        }
    }

    private AccessManager.Listener accessManagerListener() {
        return new AccessManager.Listener() {
            @Override
            public void onTokenExpired(AccessManager twilioAccessManager) {
                userPreferences.setTwilioToken(null);

                if (tokeExpiredRetries < TOKEN_EXPIRED_MAX_RETRIES) {
                    ++tokeExpiredRetries;
                    initTwilio();
                }
            }

            @Override
            public void onTokenUpdated(AccessManager twilioAccessManager) {
                userPreferences.setTwilioToken(twilioAccessManager.getToken());
                tokenErrorRetries = 0;
                tokeExpiredRetries = 0;
            }

            @Override
            public void onError(AccessManager twilioAccessManager, String s) {
                if (tokenErrorRetries < TOKEN_ERROR_MAX_RETRIES) {
                    ++tokenErrorRetries;
                    userPreferences.setTwilioToken(null);
                    initTwilio();
                }
                LogHelper.logThrowableAndCrashlytics(TAG, "accessManagerListener : Error on Token: " + s, new Throwable());
            }
        };
    }

    private TwilioConversationsClient.Listener conversationsClientListener() {
        return new TwilioConversationsClient.Listener() {
            @Override
            public void onStartListeningForInvites(TwilioConversationsClient conversationsClient) {
                LogHelper.logIfDebug(TAG, "onStartListeningForInvites");
                listenFailRetries = 0;
            }

            @Override
            public void onStopListeningForInvites(TwilioConversationsClient conversationsClient) {
                LogHelper.logIfDebug(TAG, "onStopListeningForInvites");

                if (conversationsClient != null) {
                    conversationsClient.listen();
                }
                Twilio.this.conversationsClient.listen();
            }

            @Override
            public void onFailedToStartListening(TwilioConversationsClient conversationsClient, TwilioConversationsException e) {
                LogHelper.logIfDebug(TAG, "onFailedToStartListening");

                if (e != null && e.getErrorCode() == 100 && listenFailRetries < LISTEN_FAIL_MAX_RETRIES) {
                    ++listenFailRetries;
                    initTwilio();
                }
            }

            @Override
            public void onIncomingInvite(TwilioConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                LogHelper.logIfDebug(TAG, "onIncomingInvite with conversation sid: " +
                        incomingInvite.getConversationSid() + " and invitation status: " + incomingInvite.getInviteStatus());

                LogHelper.logIfDebug(TAG, "isDuringCall: " + isDuringCall);

                if (!isDuringCall && incomingInvite.getInviteStatus() == InviteStatus.PENDING) {
                    LogHelper.logIfDebug(TAG, "starting call");
                    currentInvite = incomingInvite;

                    final String caller = String.valueOf(incomingInvite.getParticipants());
                    final String callerIdentity = caller.substring(1, caller.length() - 1);

                    profileRefreshSubject.onNext(new BothParams<>(incomingInvite.getConversationSid(), callerIdentity));
                } else if (isDuringCall) {
                    LogHelper.logIfDebug(TAG, "invitation during a call");
                    if (currentInvite == null || !incomingInvite.getInviter().equals(currentInvite.getInviter())) {
                        incomingInvite.reject();
                    } else {
                        LogHelper.logIfDebug(TAG, "Doubled invite");
                        LogHelper.logIfDebug(TAG, "Cannot start conversation with id: " + incomingInvite.getConversationSid() + " and status: " + incomingInvite.getInviteStatus());
                    }
                } else {
                    LogHelper.logIfDebug(TAG, "Cannot start conversation with id: " + incomingInvite.getConversationSid() + " and status: " + incomingInvite.getInviteStatus());
                }
            }

            @Override
            public void onIncomingInviteCancelled(TwilioConversationsClient conversationsClient, IncomingInvite incomingInvite) {
                LogHelper.logIfDebug(TAG, "onIncomingInviteCancelled with conversation sid: " +
                        incomingInvite.getConversationSid() + " and invitation status: " + incomingInvite.getInviteStatus());
                if (conversationsClient != null) {
                    conversationsClient.listen();
                }
                Twilio.this.conversationsClient.listen();
            }
        };
    }

    public void unregisterTwillio(){
        if (TwilioConversationsClient.isInitialized()) {
            TwilioConversationsClient.destroy();
        }
    }

    public void setDuringCall(boolean duringCall) {
        LogHelper.logIfDebug(TAG, "Setting during the call to: " + duringCall);
        isDuringCall = duringCall;
    }

    @Nullable
    public IncomingInvite getCurrentInvite() {
        return currentInvite;
    }

    public TwilioConversationsClient getConversationsClient() {
        return conversationsClient;
    }

    public void rejectCall(@Nonnull String twilioIdentity) {
        rejectCallSubject.onNext(twilioIdentity);
    }

    public void initCalledPersonTwilioIdentityRequest(@Nonnull String personToCallUserName) {
        initCalledPersonIdentityRequestSubject.onNext(personToCallUserName);
    }

    public Observable<String> getSuccessCalledPersonIdentity() {
        return successCalledPersonIdentity;
    }

    public Observable<Throwable> getErrorCalledPersonIdentity() {
        return errorCalledPersonIdentity;
    }

    public void clearCurrentInvite() {
        if (currentInvite != null) {
            currentInvite.reject();
            currentInvite = null;
        }
    }
}
