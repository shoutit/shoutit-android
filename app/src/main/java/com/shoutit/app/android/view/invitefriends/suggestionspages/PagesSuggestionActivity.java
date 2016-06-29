package com.shoutit.app.android.view.invitefriends.suggestionspages;

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

public class PagesSuggestionActivity extends BaseProfilesListActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, PagesSuggestionActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PagesSuggestionPresenter presenter = (PagesSuggestionPresenter) ((PagesSuggestionActivityComponent)
                getActivityComponent()).profilesListPresenter();

        presenter.getProfileToOpenObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(userName -> {
                    startActivityForResult(
                            UserOrPageProfileActivity.newIntent(PagesSuggestionActivity.this, userName),
                            REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });

        presenter.getRefreshDataObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getLoadMoreObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getListeningObservable()
                .compose(bindToLifecycle())
                .subscribe();
    }

    @Override
    protected void setUpToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.user_suggestion_pages);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@javax.annotation.Nullable final Bundle savedInstanceState) {
        PagesSuggestionActivityComponent component = DaggerPagesSuggestionActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .pagesSuggestionActivityModule(new PagesSuggestionActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();

        component.inject(this);
        return component;
    }
}
