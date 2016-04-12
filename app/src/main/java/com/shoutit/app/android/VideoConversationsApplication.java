package com.shoutit.app.android;

import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.shoutit.app.android.view.videoconversation.DialogCallActivity;
import com.shoutit.app.android.view.videoconversation.VideoConversationPresenter;
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

import rx.functions.Action1;

public class VideoConversationsApplication extends MultiDexApplication{

    private TwilioAccessManager accessManager;
    private ConversationsClient conversationsClient;
    private IncomingInvite invite;

    @Inject
    VideoConversationPresenter presenter;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    protected void InitializeVideoConversations(){
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
                                Intent intent = DialogCallActivity.newIntent(callerName, getApplicationContext());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
