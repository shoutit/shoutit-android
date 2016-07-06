package com.shoutit.app.android.view.pages.my;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.ButterKnife;


public class SelectListenersActivity extends BaseProfilesListActivity {

    public static final String RESULT_PROFILE_ID = "result_profile_id";

    public static Intent newIntent(Context context) {
        return new Intent(context, SelectListenersActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        final BaseProfileListPresenter presenter = getPresenter();

        presenter.getProfileSelectedObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(user -> {
                    setResult(RESULT_OK, new Intent().putExtra(RESULT_PROFILE_ID, user.getId()));
                    finish();
                });
    }

    private BaseProfileListPresenter getPresenter() {
        final SelectListenersActivityComponent activityComponent = (SelectListenersActivityComponent) getActivityComponent();
        return activityComponent.profilesWithoutPagesListPresenter();
    }

    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.listeners_ab_title);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final SelectListenersActivityComponent component = DaggerSelectListenersActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .selectListenersActivityModule(new SelectListenersActivityModule())
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}