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
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.twilio.Twilio;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PicassoHelper;
import com.squareup.picasso.Picasso;
import com.twilio.conversations.InviteStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DialogCallActivity extends BaseActivity {

    private static final String CALLER_NAME = "caller_name";
    private static final String CALLER_IMAGE_URL = "caller_image_url";

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

    public static Intent newIntent(@Nonnull final String callerName,
                                   @Nullable String imageUrl,
                                   @Nonnull final Context context) {
        return new Intent(context, DialogCallActivity.class)
                .putExtra(CALLER_NAME, callerName)
                .putExtra(CALLER_IMAGE_URL, imageUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_call);
        ButterKnife.bind(this);

        playRingtone(this);

        final String callerName = checkNotNull(getIntent().getStringExtra(CALLER_NAME));
        final String callerImageUrl = getIntent().getStringExtra(CALLER_IMAGE_URL);

        callInfo.setText(getString(R.string.video_calls_caller_name, callerName));

        picasso.load(callerImageUrl)
                .into(PicassoHelper.getRoundedBitmapTarget(this, calledPersonAvatarIv,
                        getResources().getDimensionPixelSize(R.dimen.call_dialog_avatar_corners)));

        RxView.clicks(acceptButton)
                .compose(bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mTwilio.getInvite() != null && mTwilio.getInvite().getInviteStatus() == InviteStatus.PENDING) {
                            startActivity(VideoConversationActivity.newIntent(callerName, null, callerImageUrl, DialogCallActivity.this));
                        } else {
                            Toast.makeText(DialogCallActivity.this, R.string.video_call_finished, Toast.LENGTH_SHORT)
                                    .show();
                        }
                        finish();
                    }
                });

        RxView.clicks(rejectButton)
                .compose(bindToLifecycle())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mTwilio.getInvite() != null) {
                            mTwilio.getInvite().reject();
                        }
                        finish();
                    }
                });
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
        super.onBackPressed();
        if (mTwilio.getInvite() != null) {
            mTwilio.getInvite().reject();
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final VideoConversationComponent component = DaggerVideoConversationComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
