package com.shoutit.app.android.view.signin.register;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
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
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.data.AssetsConstants;
import com.shoutit.app.android.utils.rx.Actions1;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.MoreFunctions1;
import com.shoutit.app.android.view.about.AboutActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.signin.LoginActivityComponent;
import com.shoutit.app.android.view.signin.login.LoginFragment;
import com.shoutit.app.android.view.webview.HtmlAssetViewerActivity;
import com.uservoice.uservoicesdk.UserVoice;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import rx.functions.Action1;

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

        passwordEdittext.setTransformationMethod(new PasswordTransformationMethod());

        setUpSpans();

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
                .compose(this.<UserLocation>bindToLifecycle())
                .subscribe();

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

        registerPresenter.failObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity()), R.string.register_error));

        registerPresenter.successObservable()
                .compose(this.<SignResponse>bindToLifecycle())
                .subscribe(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        ActivityCompat.finishAffinity(getActivity());
                        startActivity(MainActivity.newIntent(getActivity()));
                    }
                });

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
        final String bottomText1 = getString(R.string.register_bottom_text1);
        final String textTermsOfService = getString(R.string.register_bottom_text2);
        final String bottomText3 = getString(R.string.register_bottom_text3);
        final String textPrivacyPolicy = getString(R.string.register_bottom_text4);

        final SpannableString spannableTermsOfService = new SpannableString(textTermsOfService);
        spannableTermsOfService.setSpan(new ForegroundColorSpan(
                getResources().getColor(R.color.register_underline)), 0, textTermsOfService.length(), 0);
        spannableTermsOfService.setSpan(new UnderlineSpan(), 0, textTermsOfService.length(), 0);
        spannableTermsOfService.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(HtmlAssetViewerActivity.newIntent(
                        getActivity(), AssetsConstants.ASSET_TERMS_OF_SERVICE,
                        getString(R.string.html_activity_terms)));
            }
        }, 0, textTermsOfService.length(), 0);

        final SpannableString spannablePrivacy = new SpannableString(textPrivacyPolicy);
        spannablePrivacy.setSpan(new UnderlineSpan(), 0, textPrivacyPolicy.length(), 0);
        spannablePrivacy.setSpan(new ForegroundColorSpan(
                getResources().getColor(R.color.register_underline)), 0, textPrivacyPolicy.length(), 0);
        spannablePrivacy.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(HtmlAssetViewerActivity.newIntent(
                        getActivity(), AssetsConstants.ASSET_PRIVACY_POLICY,
                        getString(R.string.html_activity_privacy)));
            }
        }, 0, textPrivacyPolicy.length(), 0);

        bottomTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bottomTextView.setText(TextUtils.concat(bottomText1, " ", spannableTermsOfService, " ", bottomText3, " ", spannablePrivacy));
    }

    @OnClick(R.id.register_lock_password)
    public void lock() {
        if (passwordEdittext.getTransformationMethod() instanceof PasswordTransformationMethod) {
            passwordEdittext.setTransformationMethod(null);
        } else {
            passwordEdittext.setTransformationMethod(new PasswordTransformationMethod());
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
}