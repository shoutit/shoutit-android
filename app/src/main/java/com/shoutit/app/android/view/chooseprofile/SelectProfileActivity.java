package com.shoutit.app.android.view.chooseprofile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.functions.BothParams;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.utils.ColoredSnackBar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class SelectProfileActivity extends BaseActivity {

    public static final String RESULT_PROFILE_ID = "result_profile_id";
    public static final String RESULT_PROFILE_NAME = "result_profile_name";

    @Bind(R.id.select_profile_toolbar)
    Toolbar toolbar;
    @Bind(R.id.select_profile_view_pager)
    ViewPager viewPager;
    @Bind(R.id.select_profile_tablayout)
    TabLayout tabLayout;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    SelectProfilePagerAdapter pagerAdapter;
    @Inject
    SelectProfilePresenter presenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, SelectProfileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profile);
        ButterKnife.bind(this);

        setUpToolbar();

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getProfileSelectedObservable()
                .compose(this.<BothParams<String,String>>bindToLifecycle())
                .subscribe(new Action1<BothParams<String, String>>() {
                    @Override
                    public void call(BothParams<String, String> profileIdAndName) {
                        setResult(RESULT_OK, new Intent()
                                .putExtra(RESULT_PROFILE_ID, profileIdAndName.param1())
                                .putExtra(RESULT_PROFILE_NAME, profileIdAndName.param2()));
                        finish();
                    }
                });
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.select_profile_ab_title);
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

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final SelectProfileActivityComponent component = DaggerSelectProfileActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .selectProfileActivityModule(new SelectProfileActivityModule())
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }

}
