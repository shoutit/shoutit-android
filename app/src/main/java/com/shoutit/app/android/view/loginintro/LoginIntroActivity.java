package com.shoutit.app.android.view.loginintro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.android.MyAndroidSchedulers;
import com.appunite.rx.functions.BothParams;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.login.FacebookLogin;
import com.shoutit.app.android.api.model.login.GoogleLogin;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
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
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class LoginIntroActivity extends BaseActivity {

    private static final int GOOGLE_SIGN_IN = 0;

    @Bind(R.id.activity_login_toolbar)
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
    private Observable<UserLocation> mObservable;

    @Nonnull
    public static Intent newIntent(Context from) {
        return new Intent(from, LoginIntroActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);
        ButterKnife.bind(this);

        initFacebook();

        setUpActionBar();

        mObservable = mUserPreferences.getLocationObservable()
                .compose(this.<UserLocation>bindToLifecycle())
                .startWith((UserLocation) null)
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());

        mObservable.compose(bindToLifecycle())
                .subscribe();
    }

    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
    }

    private void setUpActionBar() {
        toolbar.setNavigationIcon(R.drawable.ic_blue_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
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
        if (requestCode == GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            final GoogleSignInAccount acct = result.getSignInAccount();
            assert acct != null;
            final String authCode = acct.getServerAuthCode();
            mObservable
                    .first()
                    .map(new Func1<UserLocation, BothParams<String, UserLocation>>() {
                        @Override
                        public BothParams<String, UserLocation> call(UserLocation location) {
                            return BothParams.of(authCode, location);
                        }
                    })
                    .flatMap(getCallGoogleApi())
                    .subscribe(getSuccessAction(), getErrorAction());
        } else {
            final boolean handled = mCallbackManager.onActivityResult(requestCode, resultCode, data);
            if (!handled) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @NonNull
    private Action1<Throwable> getErrorAction() {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                ColoredSnackBar.error(ColoredSnackBar.contentView(LoginIntroActivity.this),
                        getString(R.string.login_intro_fail),
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        };
    }

    @NonNull
    private Action1<? super SignResponse> getSuccessAction() {
        return new Action1<SignResponse>() {
            @Override
            public void call(SignResponse signResponse) {
                mUserPreferences.setLoggedIn(signResponse.getAccessToken(),
                        signResponse.getRefreshToken(), signResponse.getUser());
                ActivityCompat.finishAffinity(LoginIntroActivity.this);
                startActivity(MainActivity.newIntent(LoginIntroActivity.this));
            }
        };
    }

    @NonNull
    private Func1<BothParams<String, UserLocation>, Observable<SignResponse>> getCallGoogleApi() {
        return bothParams -> FacebookHelper.getPromotionalCodeObservable(LoginIntroActivity.this)
                .flatMap(invitationCode -> mApiService.googleLogin(new GoogleLogin(
                        bothParams.param1(), LoginUser.loginUser(bothParams.param2()), mixPanel.getDistinctId(), invitationCode))
                        .subscribeOn(Schedulers.io())
                        .observeOn(MyAndroidSchedulers.mainThread()));
    }

    @NonNull
    private Func1<BothParams<String, UserLocation>, Observable<SignResponse>> getCallFacebookApi() {
        return bothParams -> FacebookHelper.getPromotionalCodeObservable(LoginIntroActivity.this)
                .flatMap(invitationCode -> mApiService.facebookLogin(new FacebookLogin(
                        bothParams.param1(), LoginUser.loginUser(bothParams.param2()), mixPanel.getDistinctId(), invitationCode))
                        .subscribeOn(Schedulers.io())
                        .observeOn(MyAndroidSchedulers.mainThread()));
    }

    private void loginGoogle() {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode("935842257865-s6069gqjq4bvpi4rcbjtdtn2kggrvi06.apps.googleusercontent.com")
                .build();

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.activity_login_gplus_btn)
    public void googleClick() {
        loginGoogle();
    }

    @OnClick(R.id.activity_login_facebook_btn)
    public void facebookClick() {
        FacebookHelper.getToken(this, mCallbackManager)
                .withLatestFrom(mObservable, new Func2<String, UserLocation, BothParams<String, UserLocation>>() {
                    @Override
                    public BothParams<String, UserLocation> call(String s, UserLocation location) {
                        return BothParams.of(s, location);
                    }
                })
                .flatMap(getCallFacebookApi())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        progress.setVisibility(View.VISIBLE);
                    }
                })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        progress.setVisibility(View.GONE);
                    }
                })
                .subscribe(getSuccessAction(), getErrorAction());
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

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        final LoginIntroActivityComponent component = DaggerLoginIntroActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);
        return component;
    }
}
