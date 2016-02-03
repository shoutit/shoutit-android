package com.shoutit.app.android.view.intro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.UserPreferences;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.SystemUIUtils;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.login.LoginActivity;
import com.uservoice.uservoicesdk.UserVoice;
import com.viewpagerindicator.CirclePageIndicator;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroActivity extends BaseActivity {

    @Bind(R.id.activity_intro_view_pager)
    ViewPager viewPager;
    @Bind(R.id.activity_intro_page_indicators)
    CirclePageIndicator circlePageIndicator;

    @Inject
    IntroPagerAdapter pagerAdapter;

    @Inject
    UserPreferences mUserPreferences;

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
    }

    @OnClick(R.id.activity_intro_help)
    public void onHelpClick() {
        UserVoice.launchUserVoice(this);
    }

    @OnClick(R.id.activity_intro_skip)
    public void onSkipClick() {
        mUserPreferences.setGuest(true);
        startActivity(MainActivity.newIntent(this));
    }

    @OnClick(R.id.activity_intro_login_button)
    public void onLoginClick() {
        startActivity(LoginActivity.newIntent(this));
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
