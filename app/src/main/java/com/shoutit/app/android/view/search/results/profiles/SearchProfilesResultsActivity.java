package com.shoutit.app.android.view.search.results.profiles;

import android.os.Bundle;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.ButterKnife;

public class SearchProfilesResultsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profiles_results);
        ButterKnife.bind(this);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final SearchProfilesResultsActivityComponent component = DaggerSearchProfilesResultsActivity
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
