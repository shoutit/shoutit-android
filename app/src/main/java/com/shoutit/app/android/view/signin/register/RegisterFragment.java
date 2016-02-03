package com.shoutit.app.android.view.signin.register;

import android.location.Location;
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
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.Actions1;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.signin.LoginActivityComponent;
import com.shoutit.app.android.view.signin.login.LoginFragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

public class RegisterFragment extends BaseFragment {

    @Bind(R.id.register_name_edittext)
    EditText nameEditText;

    @Bind(R.id.register_email_edittext)
    EditText emailEdittext;

    @Bind(R.id.register_password_edittext)
    EditText passwordEdittext;

    @Bind(R.id.register_proceed_btn)
    Button proceedBtn;

    @Bind(R.id.register_sign_up_text)
    TextView signUpTextview;

    @Inject
    RegisterPresenter registerPresenter;

    @NonNull
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signUpTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.login_sign_layout, LoginFragment.newInstance())
                        .commit();
            }
        });

        registerPresenter.getLocationObservable()
                .compose(this.<Location>bindToLifecycle())
                .subscribe();

        registerPresenter.getEmailEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(emailEdittext, getString(R.string.register_empty_mail)));

        registerPresenter.getPasswordEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(passwordEdittext, getString(R.string.register_empty_password)));

        registerPresenter.getNameEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(nameEditText, getString(R.string.register_empty_name)));

        registerPresenter.failObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity()), R.string.register_error));

        registerPresenter.successObservable()
                .compose(this.<SignResponse>bindToLifecycle())
                .subscribe(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        final FragmentActivity activity = getActivity();
                        ActivityCompat.finishAffinity(activity);
                        startActivity(MainActivity.newIntent(activity));
                    }
                });

        RxTextView.textChangeEvents(emailEdittext)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(registerPresenter.getEmailObserver());

        RxTextView.textChangeEvents(nameEditText)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(registerPresenter.getNameObserver());

        RxTextView.textChangeEvents(passwordEdittext)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(registerPresenter.getPasswordObserver());

        RxView.clicks(proceedBtn)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(registerPresenter.getProceedObserver());
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent, @Nonnull FragmentModule fragmentModule, @Nullable Bundle savedInstanceState) {
        DaggerRegisterFragmentComponent
                .builder()
                .loginActivityComponent((LoginActivityComponent) baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}