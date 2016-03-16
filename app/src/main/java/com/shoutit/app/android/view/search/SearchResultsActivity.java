package com.shoutit.app.android.view.search;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.db.SuggestionsTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchResultsActivity extends BaseActivity {

    @Inject
    SuggestionsTable suggestionsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            saveQueryAsSuggestion();
        }
    }

    private void saveQueryAsSuggestion() {
        final Intent intent = checkNotNull(getIntent());

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            suggestionsTable.saveSuggestion(query);
        }
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final SearchResultsActivityComponent component = DaggerSearchResultsActivity
                .builder()
                .activityModule(new ActivityModule(this))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
