package com.shoutit.app.android.view.search.results.profiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.shoutit.app.android.App;
import com.shoutit.app.android.R;
import com.shoutit.app.android.api.model.BaseProfile;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.profile.ProfileIntentHelper;
import com.shoutit.app.android.view.profileslist.BaseProfileListPresenter;
import com.shoutit.app.android.view.profileslist.BaseProfilesListActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchProfilesResultsActivity extends BaseProfilesListActivity {

    private static final String KEY_SEARCH_QUERY = "search_query";

    public static Intent newIntent(Context context, @Nonnull String searchQuery) {
        return new Intent(context, SearchProfilesResultsActivity.class)
                .putExtra(KEY_SEARCH_QUERY, searchQuery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SearchProfilesResultsActivityComponent activityComponent = (SearchProfilesResultsActivityComponent) getActivityComponent();
        final BaseProfileListPresenter presenter = activityComponent.presenter();

        presenter.getProfileSelectedObservable()
                .compose(this.<BaseProfile>bindToLifecycle())
                .subscribe(baseProfile -> {
                    startActivityForResult(ProfileIntentHelper.newIntent(this, baseProfile), REQUEST_OPENED_PROFILE_WAS_LISTENED);
                });
    }

    @SuppressLint("PrivateResource")
    protected void setUpToolbar() {
        toolbar.setTitle(getString(R.string.search_profiles_results));
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String searchQuery = checkNotNull(getIntent().getStringExtra(KEY_SEARCH_QUERY));

        final SearchProfilesResultsActivityComponent component = DaggerSearchProfilesResultsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .searchProfilesResultsActivityModule(new SearchProfilesResultsActivityModule(searchQuery))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
