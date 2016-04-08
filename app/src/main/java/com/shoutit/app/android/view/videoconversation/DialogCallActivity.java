package com.shoutit.app.android.view.videoconversation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class DialogCallActivity extends BaseActivity {

    private static final String CALLER_NAME = "caller_name";

    @Bind(R.id.dialog_call_accept)
    Button acceptButton;
    @Bind(R.id.dialog_call_reject)
    Button rejectButton;
    @Bind(R.id.dialog_call_info)
    TextView callInfo;

    @Inject
    UserPreferences preferences;

    public static Intent newIntent(@Nonnull final String callerName, @Nonnull final Context context) {
        return new Intent(context, DialogCallActivity.class).putExtra(CALLER_NAME, callerName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_call);
        ButterKnife.bind(this);

        final String callerName = checkNotNull(getIntent().getStringExtra(CALLER_NAME));
        callInfo.setText(String.format(getString(R.string.video_calls_caller_name), callerName));

        RxView.clicks(acceptButton)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startActivity(VideoConversationActivity.newIntent(callerName ,null, DialogCallActivity.this));
                        finish();
                    }
                });

        RxView.clicks(rejectButton)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        ((App) getApplication()).getInvite().reject();
                        finish();
                    }
                });
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
