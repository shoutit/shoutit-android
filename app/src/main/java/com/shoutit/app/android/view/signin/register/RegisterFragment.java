package com.shoutit.app.android.view.signin.register;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.shoutit.app.android.utils.AppseeHelper;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.utils.RegisterUtils;
import com.shoutit.app.android.utils.SpanUtils;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.view.about.AboutActivity;
import com.shoutit.app.android.view.createpage.pagecategory.CreatePageCategoryActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.signin.LoginActivityComponent;
import com.shoutit.app.android.view.signin.login.LoginFragment;
import com.uservoice.uservoicesdk.UserVoice;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class RegisterFragment extends BaseFragment {

    @Bind(R.id.register_name_edittext)
    EditText nameEditText;

    @Bind(R.id.register_email_edittext)
    EditText emailEdittext;

    @Bind(R.id.register_password_edittext)
    EditText passwordEdittext;

    @Bind(R.id.register_password_layout)
    TextInputLayout passwordInputLayout;

    @Bind(R.id.register_email_layout)
    TextInputLayout emailInputLayout;

    @Bind(R.id.register_name_layout)
    TextInputLayout nameInputLayout;

    @Bind(R.id.register_proceed_btn)
    Button proceedBtn;

    @Bind(R.id.register_login_up_text)
    View signUpTextview;

    @Bind(R.id.register_bottom_text)
    TextView bottomTextView;

    @Bind(R.id.register_create_page)
    TextView mRegisterCreatePage;

    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    RegisterPresenter registerPresenter;

    @NonNull
    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @android.support.annotation.Nullable ViewGroup container, @android.support.annotation.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppseeHelper.markViewAsSensitive(emailInputLayout);
        AppseeHelper.markViewAsSensitive(passwordInputLayout);

        passwordEdittext.setTransformationMethod(new PasswordTransformationMethod());

        setUpSpans();

        signUpTextview.setOnClickListener(view1 -> getFragmentManager()
                .beginTransaction()
                .replace(R.id.login_sign_layout, LoginFragment.newInstance())
                .commit());

        registerPresenter.getEmailEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(emailInputLayout, getString(R.string.register_empty_mail)));

        registerPresenter.getPasswordEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(passwordInputLayout, getString(R.string.register_empty_password)));

        registerPresenter.getNameEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.showError(nameInputLayout, getString(R.string.register_empty_name)));

        registerPresenter.getPasswordNotEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.hideError(passwordInputLayout));

        registerPresenter.getEmailNotEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.hideError(emailInputLayout));

        registerPresenter.getNameNotEmpty()
                .compose(this.<String>bindToLifecycle())
                .subscribe(Actions1.hideError(nameInputLayout));

        registerPresenter.getProgressObservable()
                .compose(bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        registerPresenter.failObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        registerPresenter.successObservable()
                .compose(this.<SignResponse>bindToLifecycle())
                .subscribe(signResponse -> {
                    ActivityCompat.finishAffinity(getActivity());
                    startActivity(MainActivity.newIntent(getActivity()));
                });

        registerPresenter.getWrongEmailErrorObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(Actions1.showOrHideError(emailInputLayout, getString(R.string.account_wrong_email)));

        RxTextView.textChangeEvents(emailEdittext)
                .debounce(100, TimeUnit.MILLISECONDS)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(registerPresenter.getEmailObserver());

        RxTextView.textChangeEvents(nameEditText)
                .debounce(100, TimeUnit.MILLISECONDS)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(registerPresenter.getNameObserver());

        RxTextView.textChangeEvents(passwordEdittext)
                .debounce(100, TimeUnit.MILLISECONDS)
                .map(MoreFunctions1.mapTextChangeEventToString())
                .compose(this.<String>bindToLifecycle())
                .subscribe(registerPresenter.getPasswordObserver());

        RxView.clicks(proceedBtn)
                .compose(this.<Void>bindToLifecycle())
                .subscribe(registerPresenter.getProceedObserver());
    }

    private void setUpSpans() {
        RegisterUtils.setUpSpans(getActivity(), bottomTextView);

        final String createPageText = getString(R.string.register_create_page_text, getString(R.string.register_create_page_highlight));
        final String createPageClick = getString(R.string.register_create_page_highlight);
        final SpannableString createPageSpan = SpanUtils.clickableColoredSpan(createPageText,
                createPageClick, ContextCompat.getColor(getActivity(), R.color.colorAccent),
                () -> startActivity(CreatePageCategoryActivity.newIntent(getActivity(), true)));
        mRegisterCreatePage.setMovementMethod(LinkMovementMethod.getInstance());
        mRegisterCreatePage.setText(createPageSpan);
    }

    @OnCheckedChanged(R.id.register_lock_password)
    public void lock(boolean checked) {
        if (checked) {
            passwordEdittext.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            passwordEdittext.setTransformationMethod(null);

        }
        passwordEdittext.setSelection(passwordEdittext.length());
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

    @OnClick(R.id.activity_login_feedback)
    public void onFeedbackClick() {
        UserVoice.launchContactUs(getActivity());
    }

    @OnClick(R.id.activity_login_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(getActivity());
    }

    @OnClick(R.id.activity_login_about)
    public void onAboutClick() {
        startActivity(AboutActivity.newIntent(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}