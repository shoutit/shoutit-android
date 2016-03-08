package com.shoutit.app.android.view.settings.account.password;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

public class ChangePasswordFragment extends BaseFragment {

    @Bind(R.id.change_password_current_til)
    TextInputLayout currentPasswordInput;
    @Bind(R.id.change_password_password_til)
    TextInputLayout passwordInput;
    @Bind(R.id.change_password_confirm_til)
    TextInputLayout passwordConfirmInput;
    @Bind(R.id.change_password_button)
    Button confirmButton;
    @Bind(R.id.change_password_current_et)
    EditText oldPasswordEditText;
    @Bind(R.id.change_password_new_et)
    EditText passwordEditText;
    @Bind(R.id.change_password_verify_et)
    EditText passwordConfirmEditText;

    @Inject
    ChangePasswordPresenter presenter;


    public static Fragment newInstance() {
        return new ChangePasswordFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        oldPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
        passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
        passwordConfirmEditText.setTransformationMethod(new PasswordTransformationMethod());

        presenter.getRequestSuccessObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getActivity().finish();
                    }
                });

        presenter.getRequestErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getPasswordError()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(passwordInput, getString(R.string.register_empty_password)));

        presenter.getPasswordConfirmError()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(passwordConfirmInput, getString(R.string.register_empty_password)));

        presenter.getOldPasswordEmptyError()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(currentPasswordInput, getString(R.string.account_error_old_password)));

        presenter.getPasswordsDoNotMatchError()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(passwordConfirmInput, getString(R.string.settings_passwords_not_match)));

        presenter.getHadUserPasswordSetObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(currentPasswordInput));

        RxView.clicks(confirmButton)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(presenter.getConfirmClickObserver());

        RxTextView.textChangeEvents(oldPasswordEditText)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getOldPasswordObserver());

        RxTextView.textChangeEvents(passwordEditText)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getPasswordObserver());

        RxTextView.textChangeEvents(passwordConfirmEditText)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(presenter.getPasswordConfirmObserver());
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        DaggerChangePasswordFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .build()
                .inject(this);
    }
}
