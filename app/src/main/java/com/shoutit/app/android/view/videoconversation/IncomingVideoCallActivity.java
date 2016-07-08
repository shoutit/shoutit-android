package com.shoutit.app.android.view.videoconversation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.TextHelper;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.InviteStatus;
import com.twilio.conversations.TwilioConversationsException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IncomingVideoCallActivity extends VideoCallActivity {

    public static Intent newIntent(@Nullable String participantName,
                                   @Nonnull String calledUserImage,
                                   @Nonnull Context context) {
        return new Intent(context, IncomingVideoCallActivity.class)
                .putExtra(ARGS_PARTICIPANT_NAME, participantName)
                .putExtra(ARGS_CALLED_USER_IMAGE, calledUserImage);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        final String participantName = intent.getStringExtra(ARGS_PARTICIPANT_NAME);
        final String calledUserImage = intent.getStringExtra(ARGS_CALLED_USER_IMAGE);

        startActivity(IncomingVideoCallActivity.newIntent(participantName, calledUserImage, this));
    }

    @Override
    protected void initializeVideoConversations() {
        super.initializeVideoConversations();
        acceptIncomingCall();
    }

    private void acceptIncomingCall() {
        final IncomingInvite incomingInvite = mTwilio.getCurrentInvite();

        if (incomingInvite != null && incomingInvite.getInviteStatus().equals(InviteStatus.PENDING)) {
            LogHelper.logIfDebug(TAG, "VideoConAct started with conf sid: " + incomingInvite.getConversationSid());

            callButton.setVisibility(View.GONE);

            incomingInvite.accept(setupLocalMedia(), new ConversationCallback() {
                @Override
                public void onConversation(Conversation conversation, TwilioConversationsException exception) {
                    if (exception == null) {
                        IncomingVideoCallActivity.this.conversation = conversation;
                        mTwilio.setDuringCall(true);
                        conversation.setConversationListener(conversationListener());
                        showOrHideVideo(shouldShowVideo());
                    } else {
                        showError(TextHelper.formatErrorMessage(exception.getMessage()));
                    }
                }
            });
        } else {
            ColoredSnackBar.error(ColoredSnackBar.contentView(this), R.string.video_call_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected boolean isCalling() {
        return false;
    }

    @Override
    protected boolean isCaller() {
        return false;
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final IncomingVideoCallActivityComponent component = DaggerIncomingVideoCallActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }

}
