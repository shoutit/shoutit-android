package com.shoutit.app.android.view.videoconversation;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.shoutit.app.android.dagger.ForApplication;
import com.shoutit.app.android.utils.LogHelper;
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

import rx.functions.Action1;

public class VideoConversationManager {
    private static final String TAG = VideoConversationManager.class.getSimpleName();

    private TwilioAccessManager accessManager;
    private ConversationsClient conversationsClient;
    private IncomingInvite invite;
    private final VideoConversationPresenter presenter;
    @Nonnull
    private final Context context;

    public VideoConversationManager(@Nonnull VideoConversationPresenter presenter,
                                    @Nonnull @ForApplication Context context) {
        this.presenter = presenter;
        this.context = context;
    }

    public void initializeVideoConversations(){
        presenter.getTwilioRequirementObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String apiKey) {
                        initTwillio(apiKey);
                    }
                });
    }

    private void initTwillio(@Nonnull final String apiKey){
        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        if (!TwilioConversations.isInitialized()) {
            TwilioConversations.initialize(context, new TwilioConversations.InitListener() {
                @Override
                public void onInitialized() {
                    accessManager = TwilioAccessManagerFactory.createAccessManager(apiKey, accessManagerListener());
                    conversationsClient = TwilioConversations.createConversationsClient(accessManager, conversationsClientListener());
                    conversationsClient.setAudioOutput(AudioOutput.SPEAKERPHONE);
                    conversationsClient.listen();
                }
                @Override
                public void onError(Exception e) {
                    LogHelper.logThrowableAndCrashlytics(TAG, "Failed to initialize the Twilio Conversations SDK with error: ", e);
                }
            });
        }
    }

    private TwilioAccessManagerListener accessManagerListener() {
        return new TwilioAccessManagerListener() {
            @Override
            public void onAccessManagerTokenExpire(TwilioAccessManager twilioAccessManager) {
            }

            @Override
            public void onTokenUpdated(TwilioAccessManager twilioAccessManager) {
            }

            @Override
            public void onError(TwilioAccessManager twilioAccessManager, String s) {
            }
        };
    }

    private ConversationsClientListener conversationsClientListener() {
        return new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {}

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                conversationsClient.listen();
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException exception) {
                if (exception != null){
                    presenter.getTwilioRequirementObservable()
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String apiKey) {
                                    initTwillio(apiKey);
                                }
                            });
                } conversationsClient.listen();
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
                                Intent intent = DialogCallActivity.newIntent(callerName, context);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        });
            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {
            }
        };
    }

    public IncomingInvite getInvite() {
        return invite;
    }

    public ConversationsClient getConversationsClient() {
        return conversationsClient;
    }
}
