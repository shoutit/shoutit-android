package com.shoutit.app.android.view.intro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.appunite.rx.android.MyAndroidSchedulers;
import com.shoutit.app.android.BaseDaggerActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.GuestSignupRequest;
import com.shoutit.app.android.api.model.login.LoginProfile;
import com.shoutit.app.android.dagger.BaseDaggerActivityComponent;
import com.shoutit.app.android.facebook.FacebookHelper;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.SystemUIUtils;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.uservoice.uservoicesdk.UserVoice;
import com.viewpagerindicator.CirclePageIndicator;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class IntroActivity extends BaseDaggerActivity {

    public static final String EXTRA_REFRESH_TOKEN_FAILED = "refresh_token_failed";

    @Bind(R.id.activity_intro_view_pager)
    ViewPager viewPager;
    @Bind(R.id.activity_intro_page_indicators)
    CirclePageIndicator circlePageIndicator;

    @Bind(R.id.intro_progress)
    View progress;

    @Inject
    IntroPagerAdapter pagerAdapter;

    @Inject
    UserPreferences mUserPreferences;
    @Inject
    LocationManager locationManager;
    @Inject
    ApiService mApiService;
    @Inject
    MixPanel mixPanel;

    private final CompositeSubscription subscriptions = new CompositeSubscription();

    public static Intent newIntent(Context context) {
        return new Intent(context, IntroActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        SystemUIUtils.setFullscreen(this);

        if (getIntent().getBooleanExtra(EXTRA_REFRESH_TOKEN_FAILED, false)) {
            Toast.makeText(this, R.string.error_refreshing_token_failed, Toast.LENGTH_LONG).show();
        }

        viewPager.setAdapter(pagerAdapter);
        circlePageIndicator.setViewPager(viewPager);
    }

    @OnClick(R.id.activity_intro_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_intro_skip)
    public void onSkipClick() {
        mUserPreferences.setGuest(true);

        progress.setVisibility(View.VISIBLE);

        subscriptions.add(
                FacebookHelper.getPromotionalCodeObservable(IntroActivity.this)
                        .flatMap(invitationCode -> mApiService.loginGuest(
                                new GuestSignupRequest(LoginProfile.loginUser(mUserPreferences.getLocation()), mixPanel.getDistinctId(), invitationCode))
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread()))
                        .doOnTerminate(() -> progress.setVisibility(View.GONE))
                        .subscribe(signResponse -> {
                            mUserPreferences.setGuestLoggedIn(signResponse.getProfile(),
                                    signResponse.getAccessToken(),
                                    signResponse.getExpiresIn(),
                                    signResponse.getRefreshToken());
                            finish();
                            startActivity(MainActivity.newIntent(IntroActivity.this));
                        }, throwable -> {
                            ColoredSnackBar.error(ColoredSnackBar.contentView(IntroActivity.this), R.string.intro_fail_login, Snackbar.LENGTH_SHORT).show();
                        })
        );
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @OnClick(R.id.activity_intro_login_button)
    public void onLoginClick() {
        startActivity(LoginIntroActivity.newIntent(this));
    }

    @Override
    protected void injectComponent(BaseDaggerActivityComponent component) {
        component.inject(this);
    }
}
