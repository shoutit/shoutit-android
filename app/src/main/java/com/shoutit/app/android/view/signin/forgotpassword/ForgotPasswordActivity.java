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

import com.appunite.rx.android.MyAndroidSchedulers;
import com.google.common.base.Strings;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.ResetPasswordRequest;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.utils.ApiMessagesHelper;
import com.shoutit.app.android.utils.AppseeHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.about.AboutActivity;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.schedulers.Schedulers;

public class ForgotPasswordActivity extends BaseDaggerActivity {

    @Bind(R.id.forgot_password_email_edittext)
    EditText mForgotPasswordEmailEdittext;
    @Bind(R.id.forgot_password_edittext_layout)
    TextInputLayout mForgotPasswordEdittextLayout;
    @Bind(R.id.forgot_password_proceed_btn)
    Button mForgotPasswordProceedBtn;
    @Bind(R.id.forgot_password_toolbar)
    Toolbar toolbar;
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

        AppseeHelper.markViewAsSensitive(mForgotPasswordEmailEdittext);
        AppseeHelper.markViewAsSensitive(mForgotPasswordEdittextLayout);

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setLogo(R.drawable.appbar_logo_white);
        toolbar.setNavigationOnClickListener(v -> finish());

        mForgotPasswordProceedBtn.setOnClickListener(v -> {
            final String mail = mForgotPasswordEmailEdittext.getText().toString();
            if (Strings.isNullOrEmpty(mail)) {
                mForgotPasswordEdittextLayout.setError(getString(R.string.login_empty_mail));
            } else {
                mForgotPasswordEdittextLayout.setError(null);
                mForgotPasswordProgress.setVisibility(View.VISIBLE);
                mSubscription = service.resetPassword(new ResetPasswordRequest(mail))
                        .subscribeOn(Schedulers.io())
                        .observeOn(MyAndroidSchedulers.mainThread())
                        .doOnTerminate(() -> mForgotPasswordProgress.setVisibility(View.GONE))
                        .subscribe(apiMessageResponse -> {
                            ApiMessagesHelper.showApiMessageToast(ForgotPasswordActivity.this, apiMessageResponse);
                            finish();
                        }, ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(ForgotPasswordActivity.this)));
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

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
