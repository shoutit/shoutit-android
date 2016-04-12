package com.shoutit.app.android.view.verifyemail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.rx.Actions1;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class VerifyEmailActivity extends BaseActivity {

    @Bind(R.id.verify_email_toolbar)
    Toolbar toolbar;
    @Bind(R.id.verify_email_edittext)
    EditText emailEt;
    @Bind(R.id.verify_email_text_input)
    TextInputLayout emailInput;
    @Bind(R.id.verify_resend_btn)
    Button resendBtn;
    @Bind(R.id.verify_verify_btn)
    Button verifyBtn;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    VerifyEmailPresenter presenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, VerifyEmailActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        ButterKnife.bind(this);

        setUpToolbar();

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        RxView.clicks(resendBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(presenter.getResendClickObserver());

        RxView.clicks(verifyBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(presenter.getVerifyClickObserver());

        RxTextView.textChangeEvents(emailEt)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getEmailObserver());

        presenter.getEmailObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String email) {
                        emailEt.setText(email);
                        emailEt.setSelection(emailEt.getText().length());
                    }
                });

        presenter.getIsEmailVerifiedObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isProfileVerified) {
                        if (isProfileVerified) {
                            Toast.makeText(VerifyEmailActivity.this, R.string.verify_verified, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            ColoredSnackBar.error(ColoredSnackBar.contentView(VerifyEmailActivity.this),
                                    R.string.verify_not_verified, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

        presenter.getResendResponseMessage()
                .compose(this.<String>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getWrongEmailErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(emailInput, getString(R.string.account_wrong_email)));
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.verify_email_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final VerifyEmailActivityComponent component = DaggerVerifyEmailActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
