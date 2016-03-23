package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.db.RecentSearchesTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchShoutsResultsActivity extends BaseActivity {

    private static final String KEY_QUERY_TO_SAVE = "query_to_save";

    @Inject
    RecentSearchesTable recentSearchesTable;

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchShoutsResultsActivity.class);
    }

    public static Intent newIntent(Context context, @Nonnull String queryToSave) {
        return new Intent(context, SearchShoutsResultsActivity.class)
                .putExtra(KEY_QUERY_TO_SAVE, queryToSave);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            saveSearchQuery();
        }
    }

    private void saveSearchQuery() {
        final Intent intent = checkNotNull(getIntent());
        final String query = intent.getStringExtra(KEY_QUERY_TO_SAVE);
        if (!TextUtils.isEmpty(query)) {
            recentSearchesTable.saveRecentSearch(query);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final SearchShoutsResultsActivityComponent component = DaggerSearchShoutsResultsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
