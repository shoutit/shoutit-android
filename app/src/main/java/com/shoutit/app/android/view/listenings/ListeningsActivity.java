package com.shoutit.app.android.view.listenings;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.ButterKnife;

public class ListeningsActivity extends BaseProfilesListActivity {

    @Inject
    ProfilesListAdapter adapter;

    ListeningsPresenter presenter;

    public static Intent newIntent(Context context) {
        return new Intent(context, ListeningsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        presenter = (ListeningsPresenter) ((ListeningsActivityComponent) getActivityComponent()).profilesListPresenter();

        presenter.getOpenProfileObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(baseProfile -> {
                    startActivityForResult(
                            ProfileIntentHelper.newIntent(ListeningsActivity.this, baseProfile),
                            REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });
    }

    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.listenings_ab_title);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final ListeningsActivityComponent component = DaggerListeningsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .listeningsActivityModule(new ListeningsActivityModule())
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
