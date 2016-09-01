package com.shoutit.app.android.view.authorization;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseDaggerFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseDaggerFragmentComponent;
import com.shoutit.app.android.utils.AppseeHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.signin.forgotpassword.ForgotPasswordActivity;
import com.shoutit.app.android.view.signin.login.LogInSignUpPresenter;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class LogInSignUpFragment extends BaseDaggerFragment implements LogInSignUpPresenter.LogInDelegate {

    @Bind(R.id.register_email_edittext)
    EditText emailEt;
    @Bind(R.id.register_password_edittext)
    EditText passwordEt;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.register_email_edittext_layout)
    TextInputLayout emailInputLayout;
    @Bind(R.id.register_password_edittext_layout)
    TextInputLayout passwordInputLayout;
    @Bind(R.id.register_proceed_btn)
    Button proceedBtn;

    @Inject
    LogInSignUpPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_signup_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppseeHelper.markViewAsSensitive(emailInputLayout);
        AppseeHelper.markViewAsSensitive(passwordInputLayout);

        passwordEt.setTransformationMethod(new PasswordTransformationMethod());

        presenter.register(this);

        RxView.clicks(proceedBtn)
                .compose(bindToLifecycle())
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe(aVoid -> {
                    presenter.login(emailEt.getText().toString(), passwordEt.getText().toString());
                });
    }

    @OnClick(R.id.login_forgot_password_tv)
    public void onForgotPasswordClick() {
        startActivity(ForgotPasswordActivity.newIntent(getActivity()));
    }

    @OnCheckedChanged(R.id.login_lock_password)
    public void lock(boolean checked) {
        if (checked) {
            passwordEt.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            passwordEt.setTransformationMethod(null);
        }
        passwordEt.setSelection(passwordEt.length());
    }

    @Override
    protected void inject(BaseDaggerFragmentComponent component) {
        component.inject(this);
    }

    @Override
    public void showProgress(boolean show) {
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showEmailError(String message) {
        emailInputLayout.setError(message);
    }

    @Override
    public void showPasswordError(String message) {
        passwordInputLayout.setError(message);
    }

    @Override
    public void showApiError(Throwable throwable) {
        ColoredSnackBar.showError(getActivity(), throwable);
    }

    @Override
    public void showSignUpScreen() {

    }

    @Override
    public void showHomeScreen() {
        ActivityCompat.finishAffinity(getActivity());
        startActivity(MainActivity.newIntent(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unregister();
    }
}
