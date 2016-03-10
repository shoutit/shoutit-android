package com.shoutit.app.android.view.postlogininterest.postsignupsecond;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.view.main.DaggerMainActivityComponent;
import com.shoutit.app.android.view.main.MainActivity;
import com.shoutit.app.android.view.main.MainActivityComponent;
import com.shoutit.app.android.view.main.MainActivityModule;
import com.viewpagerindicator.CirclePageIndicator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostSignupSecondActivity extends BaseActivity {

    @Inject
    PostSignupSecondPresenter presenter;
    @Inject
    PostSignupPagerAdapter pagerAdapter;

    @Bind(R.id.post_signup_second_view_pager)
    ViewPager viewPager;
    @Bind(R.id.post_signup_second_view_page_indicators)
    CirclePageIndicator circlePageIndicator;
    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.post_signup_done_btn)
    Button doneButton;
    @Bind(R.id.post_signup_next_btn)
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_signup_second);
        ButterKnife.bind(this);

        viewPager.setAdapter(pagerAdapter);
        circlePageIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                doneButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.GONE);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));
    }


    @OnClick(R.id.post_signup_next_btn)
    public void onNextClicked() {
        viewPager.setCurrentItem(1);
        doneButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
    }

    @OnClick({R.id.post_signup_done_btn, R.id.post_signup_skip_btn})
    public void onDoneOrSkipClicked() {
        finish();
        startActivity(MainActivity.newIntent(this));
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final PostSignupSecondActivityComponent component = DaggerPostSignupSecondActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .postSignupSecondActivityModule(new PostSignupSecondActivityModule())
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
