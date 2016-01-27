package com.shoutit.app.android.view.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.SystemUIUtils;
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
        // TODO
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_LONG).show();
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
