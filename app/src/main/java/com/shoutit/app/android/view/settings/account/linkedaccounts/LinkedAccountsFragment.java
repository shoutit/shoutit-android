package com.shoutit.app.android.view.settings.account.linkedaccounts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.loginintro.FacebookHelper;
import com.shoutit.app.android.view.loginintro.GoogleHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class LinkedAccountsFragment extends BaseFragment implements LinkedAccountsPresenter.Listener {

    private static final int GOOGLE_SIGN_IN = 0;

    @Bind(R.id.linked_accounts_facebook_tv)
    TextView linkedAccountsFacebookTv;
    @Bind(R.id.linked_accounts_profile_fb_tv)
    TextView linkedAccountsProfileFbTv;
    @Bind(R.id.linked_accounts_google_tv)
    TextView linkedAccountsGoogleTv;
    @Bind(R.id.linked_accounts_profile_g_tv)
    TextView linkedAccountsProfileGTv;
    @Bind(R.id.main_layout)
    View mainView;

    @Inject
    LinkedAccountsPresenter presenter;
    @Inject
    UserPreferences preferences;

    private CallbackManager callbackManager;
    private String googleId;

    public static Fragment newInstance() {
        return new LinkedAccountsFragment();
    }

    @android.support.annotation.Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @android.support.annotation.Nullable final ViewGroup container, @android.support.annotation.Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_linked_accounts, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.register(this);

        callbackManager = CallbackManager.Factory.create();

        RxView.clicks(linkedAccountsFacebookTv)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.clickFacebookSubject());

        RxView.clicks(linkedAccountsGoogleTv)
                .compose(this.bindToLifecycle())
                .subscribe(presenter.clickGoogleSubject());

        presenter.facebookLinkedInfoObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(linkedAccountsProfileFbTv));

        presenter.googleLinkedInfoObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(RxTextView.text(linkedAccountsProfileGTv));

        presenter.askForFbTokenObservable()
                .flatMap(o -> FacebookHelper.getToken(getActivity(), callbackManager))
                .subscribe(onGetFacebookTokenSuccessAction(), onGetFacebookTokenFailureAction());

        presenter.linkFacebookObservable()
                .compose(this.bindToLifecycle())
                .subscribe(ColoredSnackBar.successSnackBarAction(mainView));

        presenter.unlinkFacebookObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(message -> {
                    preferences.setUserOrPage(preferences.getUser().withUnlinkedFacebook());
                    ColoredSnackBar.success(mainView, message, Snackbar.LENGTH_SHORT).show();
                });

        presenter.linkGoogleObservable()
                .compose(this.bindToLifecycle())
                .subscribe(message -> {
                    preferences.setUserOrPage(preferences.getUser().withUpdatedGoogleAccount(googleId));
                    ColoredSnackBar.success(mainView, message, Snackbar.LENGTH_SHORT).show();
                });

        presenter.unlinkGoogleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(message -> {
                    preferences.setUserOrPage(preferences.getUser().withUnlinkedGoogle());
                    ColoredSnackBar.success(mainView, message, Snackbar.LENGTH_SHORT).show();
                });

        presenter.errorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(mainView));
    }

    @Nonnull
    private Action1<String> onGetFacebookTokenSuccessAction() {
        return token -> presenter.linkFacebookSubject().onNext(token);
    }

    @NonNull
    private Action1<Throwable> onGetFacebookTokenFailureAction() {
        return throwable -> ColoredSnackBar.error(ColoredSnackBar.contentView(getActivity()), throwable,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == GOOGLE_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            final GoogleSignInAccount acct = result.getSignInAccount();

            if (acct != null && acct.getId() != null) {
                googleId = acct.getId();
                presenter.linkGoogleSubject().onNext(acct.getServerAuthCode());
            }

        } else {
            final boolean handled = callbackManager.onActivityResult(requestCode, resultCode, data);
            if (!handled) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void triggerSignInGoogle() {
        GoogleHelper.loginGoogle(getActivity(), GOOGLE_SIGN_IN);
    }

    @Override
    public void unlinkFacebookDialog() {
        showDialog(getString(R.string.linked_accounts_unlink_facebook), presenter.unlinkFacebookSubject());
    }

    @Override
    public void unlinkGoogleDialog() {
        showDialog(getString(R.string.linked_accounts_unlink_google), presenter.unlinkGoogleSubject());
    }

    private void showDialog(@Nonnull final String message, @Nonnull PublishSubject<Object> subject) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("yes", (dialog, which) -> {
                    subject.onNext(new Object());
                })
                .setNegativeButton("no", (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.unsubscribe();
    }

    @Override
    protected void injectComponent(@Nonnull final BaseActivityComponent baseActivityComponent, @Nonnull final FragmentModule fragmentModule, @Nullable final Bundle savedInstanceState) {
        DaggerLinkedAccountsComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(new FragmentModule(this))
                .build()
                .inject(this);
    }
}
