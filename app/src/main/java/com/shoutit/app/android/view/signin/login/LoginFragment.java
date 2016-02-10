package com.shoutit.app.android.view.signin.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.Actions1;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.signin.LoginActivityComponent;
import com.shoutit.app.android.view.signin.register.RegisterFragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import okhttp3.ResponseBody;
import rx.functions.Action1;
import rx.functions.Func1;

public class LoginFragment extends BaseFragment {

    @Bind(R.id.register_email_edittext)
    EditText emailEdittext;

    @Bind(R.id.register_password_edittext)
    EditText passwordEdittext;

    @Bind(R.id.register_proceed_btn)
    Button proceedBtn;

    @Bind(R.id.login_sign_up_text)
    View signUp;

    @Bind(R.id.login_forgot_password_tv)
    TextView forgotPassworTextView;

    @Bind(R.id.fragment_login_progress)
    View progressView;

    @Inject
    LoginPresenter loginPresenter;

    @NonNull
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.login_sign_layout, RegisterFragment.newInstance())
                        .commit();
            }
        });

        loginPresenter.getLocationObservable()
                .compose(this.<UserLocation>bindToLifecycle())
                .subscribe();

        loginPresenter.getEmailEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(emailEdittext, getString(R.string.login_empty_mail)));

        loginPresenter.getPasswordEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(passwordEdittext, getString(R.string.login_empty_password)));

        loginPresenter.failObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        loginPresenter.successObservable()
                .compose(this.<SignResponse>bindToLifecycle())
                .subscribe(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        final FragmentActivity activity = getActivity();
                        ActivityCompat.finishAffinity(activity);
                        startActivity(MainActivity.newIntent(activity));
                    }
                });

        loginPresenter.successResetPassword()
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(
                        ColoredSnackBar.contentView(getActivity()), R.string.login_success_reset_password));

        loginPresenter.resetPasswordEmptyEmail()
                .compose(this.bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(
                        ColoredSnackBar.contentView(getActivity()), R.string.login_error_empty_email));

        RxTextView.textChangeEvents(emailEdittext)
                .map(new Func1<TextViewTextChangeEvent, String>() {
                    @Override
                    public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return textViewTextChangeEvent.text().toString();
                    }
                })
                .compose(this.<String>bindToLifecycle())
                .subscribe(loginPresenter.getEmailObserver());

        RxTextView.textChangeEvents(passwordEdittext)
                .map(new Func1<TextViewTextChangeEvent, String>() {
                    @Override
                    public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return textViewTextChangeEvent.text().toString();
                    }
                })
                .compose(this.<String>bindToLifecycle())
                .subscribe(loginPresenter.getPasswordObserver());

        RxView.clicks(proceedBtn)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(loginPresenter.getProceedObserver());

        RxView.clicks(forgotPassworTextView)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(loginPresenter.getResetPasswordClickObserver());

        loginPresenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent, @Nonnull FragmentModule fragmentModule, @Nullable Bundle savedInstanceState) {
        DaggerLoginFragmentComponent
                .builder()
                .loginActivityComponent((LoginActivityComponent) baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}