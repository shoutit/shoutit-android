package com.shoutit.app.android.view.invitefriends.suggestionsusers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profile.UserOrPageProfileActivity;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;

public class UserSuggestionActivity extends BaseProfilesListActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, UserSuggestionActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final UserSuggestionPresenter presenter = (UserSuggestionPresenter) ((UserSuggestionActivityComponent)
                getActivityComponent()).profilesListPresenter();

        presenter.getProfileSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(userName -> {
                    startActivityForResult(
                            UserOrPageProfileActivity.newIntent(UserSuggestionActivity.this, userName),
                            REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });
    }

    @Override
    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.user_suggestion_users);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable Bundle savedInstanceState) {
        UserSuggestionActivityComponent component = DaggerUserSuggestionActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .userSuggestionActivityModule(new UserSuggestionActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();

        component.inject(this);
        return component;
    }
}