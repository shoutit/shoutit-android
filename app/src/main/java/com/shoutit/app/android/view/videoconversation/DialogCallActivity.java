package com.shoutit.app.android.view.videoconversation;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.LogHelper;
import com.shoutit.app.android.utils.PicassoHelper;
import com.squareup.picasso.Picasso;
import com.twilio.conversations.IncomingInvite;
import com.twilio.conversations.InviteStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DialogCallActivity extends BaseDaggerActivity {

    private final String TAG = DialogCallActivity.class.getSimpleName();
    private static final String CALLER_NAME = "caller_name";
    private static final String CALLER_IMAGE_URL = "caller_image_url";
    private static final String CONVERSATION_ID = "conversation_id";

    @Bind(R.id.dialog_call_accept)
    View acceptButton;
    @Bind(R.id.dialog_call_reject)
    View rejectButton;
    @Bind(R.id.dialog_call_info)
    TextView callInfo;
    @Bind(R.id.dialog_call_avatar_iv)
    ImageView calledPersonAvatarIv;

    @Inject
    UserPreferences preferences;
    @Inject
    Twilio mTwilio;
    @Inject
    Picasso picasso;

    private String conversationId;

    public static Intent newIntent(@Nonnull String callerName,
                                   @Nullable String imageUrl,
                                   @Nonnull String conversationId,
                                   @Nonnull Context context) {
        return new Intent(context, DialogCallActivity.class)
                .putExtra(CONVERSATION_ID, conversationId)
                .putExtra(CALLER_NAME, callerName)
                .putExtra(CALLER_IMAGE_URL, imageUrl)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        finish();
        final String callerName = checkNotNull(getIntent().getStringExtra(CALLER_NAME));
        final String callerImageUrl = getIntent().getStringExtra(CALLER_IMAGE_URL);
        conversationId = checkNotNull(getIntent().getStringExtra(CONVERSATION_ID));

        startActivity(DialogCallActivity.newIntent(callerName, callerImageUrl,conversationId, this));
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_call);
        ButterKnife.bind(this);

        playRingtone(this);

        final String callerName = checkNotNull(getIntent().getStringExtra(CALLER_NAME));
        final String callerImageUrl = getIntent().getStringExtra(CALLER_IMAGE_URL);
        conversationId = checkNotNull(getIntent().getStringExtra(CONVERSATION_ID));

        callInfo.setText(getString(R.string.video_calls_caller_name, callerName));

        picasso.load(callerImageUrl)
                .into(PicassoHelper.getRoundedBitmapTarget(this, calledPersonAvatarIv,
                        getResources().getDimensionPixelSize(R.dimen.call_dialog_avatar_corners)));

        RxView.clicks(acceptButton)
                .compose(bindToLifecycle())
                .subscribe(aVoid -> {
                    if (canAnswerTheCall()) {
                        LogHelper.logIfDebug(TAG, "starting conversation with conv sid: " + conversationId);
                        startActivity(IncomingVideoCallActivity.newIntent(callerName, callerImageUrl, DialogCallActivity.this));
                    } else {
                        Toast.makeText(DialogCallActivity.this, R.string.video_call_finished, Toast.LENGTH_SHORT)
                                .show();
                    }
                    finish();
                });

        RxView.clicks(rejectButton)
                .compose(bindToLifecycle())
                .subscribe(aVoid -> {
                    rejectCall();
                    finish();
                });
    }

    private boolean canAnswerTheCall() {
        final IncomingInvite currentInvite = mTwilio.getCurrentInvite();
        return currentInvite != null &&
                currentInvite.getInviteStatus() == InviteStatus.PENDING;
    }

    private void playRingtone(Context context) {
        try {
            final Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            final Ringtone ringtone = RingtoneManager.getRingtone(context, notification);

            final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            ringtone.play();
            vibrator.vibrate(1000);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onBackPressed() {
        rejectCall();
        super.onBackPressed();
    }

    private void rejectCall() {
        if (mTwilio.getCurrentInvite() != null) {
            mTwilio.getCurrentInvite().reject();
        }
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
