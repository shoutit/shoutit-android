package com.shoutit.app.android.view.signin.forgotpassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.google.common.base.Strings;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ResetPasswordRequest;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.RtlUtils;
import com.shoutit.app.android.view.about.AboutActivity;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ForgotPasswordActivity extends BaseActivity {

    @Bind(R.id.forgot_password_email_edittext)
    EditText mForgotPasswordEmailEdittext;
    @Bind(R.id.forgot_password_edittext_layout)
    TextInputLayout mForgotPasswordEdittextLayout;
    @Bind(R.id.forgot_password_proceed_btn)
    Button mForgotPasswordProceedBtn;
    @Bind(R.id.forgot_password_toolbar)
    Toolbar mForgotPasswordToolbar;
    @Bind(R.id.forgot_password_progress)
    FrameLayout mForgotPasswordProgress;

    @Inject
    ApiService service;
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);
        ButterKnife.bind(this);

        mForgotPasswordToolbar.setNavigationIcon(RtlUtils.isRtlEnabled(this) ?
                R.drawable.ic_blue_arrow_rtl : R.drawable.ic_blue_arrow);
        mForgotPasswordToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mForgotPasswordProceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mail = mForgotPasswordEmailEdittext.getText().toString();
                if (Strings.isNullOrEmpty(mail)) {
                    mForgotPasswordEdittextLayout.setError(getString(R.string.login_empty_mail));
                } else {
                    mForgotPasswordEdittextLayout.setError(null);
                    mForgotPasswordProgress.setVisibility(View.VISIBLE);
                    mSubscription = service.resetPassword(new ResetPasswordRequest(mail))
                            .subscribeOn(Schedulers.io())
                            .observeOn(MyAndroidSchedulers.mainThread())
                            .doOnTerminate(new Action0() {
                                @Override
                                public void call() {
                                    mForgotPasswordProgress.setVisibility(View.GONE);
                                }
                            })
                            .subscribe(new Action1<ResponseBody>() {
                                @Override
                                public void call(ResponseBody responseBody) {
                                    finish();
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    ColoredSnackBar
                                            .error(
                                                    ColoredSnackBar.contentView(ForgotPasswordActivity.this),
                                                    R.string.forgot_password_fail,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ForgotPasswordActivityComponent component = DaggerForgotPasswordActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);
        return component;
    }

    @Nonnull
    public static Intent newIntent(Context context) {
        return new Intent(context, ForgotPasswordActivity.class);
    }

    @OnClick(R.id.activity_login_feedback)
    public void onFeedbackClick() {
        UserVoice.launchContactUs(this);
    }

    @OnClick(R.id.activity_login_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_login_about)
    public void onAboutClick() {
        startActivity(AboutActivity.newIntent(this));
    }
}
