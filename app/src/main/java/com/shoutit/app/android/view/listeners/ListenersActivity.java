package com.shoutit.app.android.view.listeners;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;
import com.shoutit.app.android.view.listenings.ProfilesListAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListenersActivity extends BaseProfilesListActivity {

    private static final String KEY_USER_NAME = "user_name";

    @Inject
    ProfilesListAdapter adapter;

    ListenersPresenter presenter;

    public static Intent newIntent(Context context, @Nonnull String userName) {
        return new Intent(context, ListenersActivity.class)
                .putExtra(KEY_USER_NAME, userName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        presenter = (ListenersPresenter) ((ListenersActivityComponent) getActivityComponent()).profilesListPresenter();

        presenter.getProfileSelectedObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(baseProfile -> {
                        startActivityForResult(
                                ProfileIntentHelper.newIntent(ListenersActivity.this, baseProfile.getUsername(), baseProfile.isPage()),
                                REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });
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
        final String userName = checkNotNull(getIntent().getStringExtra(KEY_USER_NAME));

        final ListenersActivityComponent component = DaggerListenersActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .listenersActivityModule(new ListenersActivityModule(userName))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
