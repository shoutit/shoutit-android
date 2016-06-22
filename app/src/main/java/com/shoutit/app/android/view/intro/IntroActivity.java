package com.shoutit.app.android.view.intro;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.appunite.rx.ObservableExtensions;
import com.appunite.rx.android.MyAndroidSchedulers;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.api.ApiService;
import com.shoutit.app.android.api.model.GuestSignupRequest;
import com.shoutit.app.android.api.model.SignResponse;
import com.shoutit.app.android.api.model.UserLocation;
import com.shoutit.app.android.api.model.login.LoginUser;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.location.LocationManager;
import com.shoutit.app.android.mixpanel.MixPanel;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.PermissionHelper;
import com.shoutit.app.android.utils.SystemUIUtils;
import com.shoutit.app.android.view.loginintro.LoginIntroActivity;
import com.shoutit.app.android.view.main.MainActivity;
import com.uservoice.uservoicesdk.UserVoice;
import com.viewpagerindicator.CirclePageIndicator;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class IntroActivity extends BaseActivity {

    private static final int REQUEST_CODE_LOCATION = 1;

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

    private Observable<UserLocation> mLocationObservable;

    public static Intent newIntent(Context context) {
        return new Intent(context, IntroActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
        ((IntroActivityComponent) getActivityComponent()).inject(this);
        SystemUIUtils.setFullscreen(this);

        viewPager.setAdapter(pagerAdapter);
        circlePageIndicator.setViewPager(viewPager);

        askForLocationPermissionIfNeeded();

        mLocationObservable = mUserPreferences.getLocationObservable()
                .startWith((UserLocation) null)
                .compose(ObservableExtensions.<UserLocation>behaviorRefCount());
        mLocationObservable
                .compose(bindToLifecycle())
                .subscribe();
    }

    private void askForLocationPermissionIfNeeded() {
        PermissionHelper.checkPermissions(this, REQUEST_CODE_LOCATION,
                findViewById(android.R.id.content), R.string.permission_location_explanation,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
    }

    @OnClick(R.id.activity_intro_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_intro_skip)
    public void onSkipClick() {
        mUserPreferences.setGuest(true);

        progress.setVisibility(View.VISIBLE);
        mLocationObservable.first()
                .flatMap(new Func1<UserLocation, Observable<SignResponse>>() {
                    @Override
                    public Observable<SignResponse> call(UserLocation location) {
                        return mApiService.loginGuest(new GuestSignupRequest(LoginUser.loginUser(location), mixPanel.getDistinctId()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(MyAndroidSchedulers.mainThread());
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        progress.setVisibility(View.GONE);
                    }
                })
                .subscribe(new Action1<SignResponse>() {
                    @Override
                    public void call(SignResponse signResponse) {
                        mUserPreferences.setGuestLoggedIn(signResponse.getUser(), signResponse.getAccessToken(), signResponse.getRefreshToken());
                        finish();
                        startActivity(MainActivity.newIntent(IntroActivity.this));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ColoredSnackBar.error(ColoredSnackBar.contentView(IntroActivity.this), R.string.intro_fail_login, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.activity_intro_login_button)
    public void onLoginClick() {
        startActivity(LoginIntroActivity.newIntent(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            final boolean permissionsGranted = PermissionHelper.arePermissionsGranted(grantResults);
            if (permissionsGranted) {
                ColoredSnackBar.success(findViewById(android.R.id.content), R.string.permission_granted, Snackbar.LENGTH_SHORT).show();
                locationManager.getRefreshGetLocationSubject().onNext(null);
            } else {
                ColoredSnackBar.error(findViewById(android.R.id.content), R.string.permission_not_granted, Snackbar.LENGTH_SHORT);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        return DaggerIntroActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
    }
}
