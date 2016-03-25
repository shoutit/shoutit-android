package com.shoutit.app.android.view.search.results.shouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.search.SearchPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchShoutsResultsActivity extends BaseActivity {

    public static final String KEY_SEARCH_QUERY = "query_to_save";
    public static final String KEY_CONTEXTUAL_ITEM_ID = "contextual_item_id";
    public static final String KEY_SEARCH_TYPE = "search_type";

    @Bind(R.id.search_shouts_results_toolbar)
    Toolbar toolbar;

    public static Intent newIntent(Context context, @Nullable String queryToSave,
                                   @Nullable String contextualItemId,
                                   @Nonnull SearchPresenter.SearchType searchType) {
        return new Intent(context, SearchShoutsResultsActivity.class)
                .putExtra(KEY_SEARCH_QUERY, queryToSave)
                .putExtra(KEY_CONTEXTUAL_ITEM_ID, contextualItemId)
                .putExtra(KEY_SEARCH_TYPE, searchType);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_shoutsresults);
        ButterKnife.bind(this);

        final Intent intent = checkNotNull(getIntent());
        final String searchQuery = checkNotNull(intent.getStringExtra(KEY_SEARCH_QUERY));
        final String contextualItemId = intent.getStringExtra(KEY_CONTEXTUAL_ITEM_ID);
        final SearchPresenter.SearchType searchType = (SearchPresenter.SearchType) checkNotNull(intent.getSerializableExtra(KEY_SEARCH_TYPE));

        setUpToolbar(searchQuery);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_search_results_container,
                            SearchShoutsResultsFragment.newInstance(searchQuery, contextualItemId, searchType))
                    .commit();
        }
    }

    @SuppressLint("PrivateResource")
    private void setUpToolbar(String title) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.inflateMenu(R.menu.menh_search_shouts_results);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search_results_menu_search:
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
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
