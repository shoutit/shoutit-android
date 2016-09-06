package com.shoutit.app.android.view.loginintro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.login.FacebookLogin;
import com.shoutit.app.android.api.model.login.GoogleLogin;
import com.shoutit.app.android.api.model.login.LoginProfile;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.about.AboutActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.signin.LoginActivity;
import com.uservoice.uservoicesdk.UserVoice;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LoginIntroActivity extends BaseDaggerActivity {

    public static final int GOOGLE_SIGN_IN = 0;

    @Bind(R.id.signups_toolbar)
    Toolbar toolbar;

    @Bind(R.id.activity_login_progress_layout)
    View progress;

    @Inject
    ApiService mApiService;
    @Inject
    UserPreferences mUserPreferences;
    @Inject
    MixPanel mixPanel;

    private CallbackManager mCallbackManager;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Nonnull
    public static Intent newIntent(Context from) {
        return new Intent(from, LoginIntroActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);
        ButterKnife.bind(this);

        toolbar.setElevation(0);

        initFacebook();

        setUpActionBar();
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
    }

    private void setUpActionBar() {
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GOOGLE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                final GoogleSignInAccount acct = result.getSignInAccount();
                assert acct != null;
                final String authCode = acct.getServerAuthCode();

                subscriptions.add(
                        FacebookHelper.getPromotionalCodeObservable(LoginIntroActivity.this)
                                .flatMap(invitationCode -> mApiService.googleLogin(new GoogleLogin(
                                        authCode, LoginProfile.loginUser(mUserPreferences.getLocation()), mixPanel.getDistinctId(), invitationCode))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(MyAndroidSchedulers.mainThread()))
                                .subscribe(getSuccessAction(), getErrorAction())
                );
            } else {
                ColoredSnackBar.error(ColoredSnackBar.contentView(LoginIntroActivity.this),
                        getString(R.string.login_intro_fail),
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            final boolean handled = mCallbackManager.onActivityResult(requestCode, resultCode, data);
            if (!handled) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @NonNull
    private Action1<Throwable> getErrorAction() {
        return throwable -> ColoredSnackBar.error(ColoredSnackBar.contentView(LoginIntroActivity.this),
                getString(R.string.login_intro_fail),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @NonNull
    private Action1<? super SignResponse> getSuccessAction() {
        return signResponse -> {
            mUserPreferences.setLoggedIn(signResponse.getAccessToken(), signResponse.getExpiresIn(),
                    signResponse.getRefreshToken(), signResponse.getProfile());
            ActivityCompat.finishAffinity(LoginIntroActivity.this);
            startActivity(MainActivity.newIntent(LoginIntroActivity.this));
        };
    }

    @OnClick(R.id.activity_login_gplus_btn)
    public void googleClick() {
        GoogleHelper.loginGoogle(this, GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.activity_login_facebook_btn)
    public void facebookClick() {
        subscriptions.add(
                FacebookHelper.getToken(this, mCallbackManager)
                        .switchMap(token -> FacebookHelper.getPromotionalCodeObservable(LoginIntroActivity.this)
                                .switchMap(invitationCode -> mApiService.facebookLogin(new FacebookLogin(
                                        token, LoginProfile.loginUser(mUserPreferences.getLocation()), mixPanel.getDistinctId(), invitationCode))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(MyAndroidSchedulers.mainThread())))
                        .doOnSubscribe(() -> progress.setVisibility(View.VISIBLE))
                        .finallyDo(() -> progress.setVisibility(View.GONE))
                        .subscribe(getSuccessAction(), getErrorAction())
        );
    }

    @OnClick(R.id.activity_login_signup)
    public void singUpClick() {
        startActivity(LoginActivity.newIntent(this));
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
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
