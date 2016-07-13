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
import com.shoutit.app.android.view.profile.tagprofile.TagProfileActivity;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListeningsActivity extends BaseProfilesListActivity {

    private static final String KEY_ARE_INTERESTS = "are_interests";

    @Inject
    ProfilesListAdapter adapter;

    ListeningsPresenter presenter;

    public static Intent newIntent(Context context, boolean areInterests) {
        return new Intent(context, ListeningsActivity.class)
                .putExtra(KEY_ARE_INTERESTS, areInterests);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        final boolean areInterests = checkNotNull(getIntent().getBooleanExtra(KEY_ARE_INTERESTS, false));

        presenter = (ListeningsPresenter) ((ListeningsActivityComponent) getActivityComponent()).profilesListPresenter();

        presenter.getOpenProfileObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(baseProfile -> {
                    if (areInterests) {
                        startActivityForResult(
                                TagProfileActivity.newIntent(ListeningsActivity.this, baseProfile.getName()),
                                REQUEST_OPENED_PROFILE_WAS_LISTENED);
                    } else {
                        startActivityForResult(
                                ProfileIntentHelper.newIntent(ListeningsActivity.this, baseProfile),
                                REQUEST_OPENED_PROFILE_WAS_LISTENED);
                    }
                });
    }

    protected void setUpToolbar() {
        final boolean areInterests = checkNotNull(getIntent().getBooleanExtra(KEY_ARE_INTERESTS, false));

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(areInterests ?
                R.string.listenings_interests_ab_title : R.string.listenings_ab_title);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final boolean areInterests = checkNotNull(getIntent().getBooleanExtra(KEY_ARE_INTERESTS, false));
        final ListeningsPresenter.ListeningsType listeningsType = areInterests ?
                ListeningsPresenter.ListeningsType.INTERESTS : ListeningsPresenter.ListeningsType.USERS_AND_PAGES;

        final ListeningsActivityComponent component = DaggerListeningsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .listeningsActivityModule(new ListeningsActivityModule(listeningsType))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
