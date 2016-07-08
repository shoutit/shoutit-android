package com.shoutit.app.android.view.videoconversation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.appunite.rx.functions.BothParams;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.OutgoingInvite;
import com.twilio.conversations.TwilioConversationsException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.functions.Action1;

public class OutgoingVideoCallActivity extends VideoCallActivity {

    private static final String ARGS_CALLED_USER_USERNAME = "caled_user_username";

    private OutgoingInvite outgoingInvite;
    private String calledUserTwilioIdentity;

    public static Intent newIntent(@Nullable String participantName,
                                   @Nullable String calledUserUsername,
                                   @Nonnull String calledUserImage,
                                   @Nonnull Context context) {
        return new Intent(context, OutgoingVideoCallActivity.class)
                .putExtra(ARGS_CALLED_USER_USERNAME, calledUserUsername)
                .putExtra(ARGS_CALLED_USER_IMAGE, calledUserImage)
                .putExtra(ARGS_PARTICIPANT_NAME, participantName);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        final String calledUserUsername = intent.getStringExtra(ARGS_CALLED_USER_USERNAME);
        final String participantName = intent.getStringExtra(ARGS_PARTICIPANT_NAME);
        final String calledUserImage = intent.getStringExtra(ARGS_CALLED_USER_IMAGE);

        startActivity(OutgoingVideoCallActivity.newIntent(participantName, calledUserUsername, calledUserImage, this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String calledUserUsername = getIntent().getStringExtra(ARGS_CALLED_USER_USERNAME);

        RxView.clicks(callButton)
                .throttleFirst(5, TimeUnit.SECONDS)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(ignore -> {
                    mTwilio.initCalledPersonTwilioIdentityRequest(calledUserUsername);
                    showConversationInfo(getString(R.string.video_call_calling));
                });

        mTwilio.getSuccessCalledPersonIdentity()
                .take(1)
                .compose(this.<String>bindToLifecycle())
                .subscribe(calledUserIdentity -> {
                    calledUserTwilioIdentity = calledUserIdentity;
                    presenter.getCalledUserTwilioIdentityObserver().onNext(calledUserIdentity);
                    initOutgoingCall();
                });

        presenter.getMakeCallObservable()
                .compose(this.<BothParams<Set<String>, Boolean>>bindToLifecycle())
                .subscribe(makeOutgoingCall());
    }

    @Override
    protected boolean isCalling() {
        return conversation == null && isCaller() && outgoingInvite != null;
    }

    @Override
    protected boolean isCaller() {
        return true;
    }

    private void initOutgoingCall() {

        if (calledUserTwilioIdentity != null && conversationClient != null) {
            callButton.setVisibility(View.GONE);

            final Set<String> participants = new HashSet<>();
            participants.add(calledUserTwilioIdentity);

            presenter.getParticipantsObserver().onNext(participants);
            presenter.getMakeOutgoingCallObserver().onNext(null);
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this),
                    R.string.video_calls_cannot_start_conversation,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @NonNull
    private Action1<BothParams<Set<String>, Boolean>> makeOutgoingCall() {
        return new Action1<BothParams<Set<String>, Boolean>>() {
            @Override
            public void call(BothParams<Set<String>, Boolean> participantsWithIsLastRetry) {
                final Set<String> participants = participantsWithIsLastRetry.param1();
                final Boolean isLastRetry = participantsWithIsLastRetry.param2();

                smallPreviewWindow.setVisibility(View.VISIBLE);
                smallPreviewCoverView.setVisibility(View.VISIBLE);
                showOrHideSmallPreview(true);

                LogHelper.logIfDebug(TAG, "Send outgoing invite");
                outgoingInvite = conversationClient
                        .inviteToConversation(participants, setupLocalMedia(), new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                                if (exception == null) {
                                    presenter.finishRetries();
                                    mTwilio.setDuringCall(true);
                                    OutgoingVideoCallActivity.this.conversation = conversation;
                                    conversation.setConversationListener(conversationListener());
                                    LogHelper.logIfDebug(TAG, "Succesfully connected");
                                } else if (isLastRetry) {
                                    conversationFinishedWithError(exception);
                                    LogHelper.logIfDebug(TAG, "Failed on last retry with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                } else if (exception.getErrorCode() == Twilio.ERROR_PARTICIPANT_UNAVAILABLE) {
                                    LogHelper.logIfDebug(TAG, "Participant unavailable? with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                    presenter.retryCall();
                                } else {
                                    LogHelper.logIfDebug(TAG, "Error? with code: " + exception.getErrorCode() + " and message: " + exception.getMessage());
                                    conversationFinishedWithError(exception);
                                    presenter.finishRetries();
                                }
                            }
                        });
            }
        };
    }

    private void conversationFinishedWithError(TwilioConversationsException exception) {
        switchToFullScreenMode(false);
        showOrHideSmallPreview(false);
        handleConversationError(exception);
        mTwilio.rejectCall(calledUserTwilioIdentity);
    }

    protected void handleConversationError(TwilioConversationsException exception) {
        if (exception.getErrorCode() == Twilio.ERROR_PARTICIPANT_REJECTED_CALL) {
            showConversationInfo(getString(R.string.video_calls_participant_reject));
        } else {
            showConversationInfo(TextHelper.formatErrorMessage(exception.getMessage()));
        }
    }

    @Override
    protected void closeConversation() {
        super.closeConversation();

        if (outgoingInvite != null) {
            outgoingInvite.cancel();
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final OutgoingVideoCallActivityComponent component = DaggerOutgoingVideoCallActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
