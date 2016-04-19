package com.shoutit.app.android.view.search.results.shouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.main.MainSearchActivity;
import com.shoutit.app.android.view.search.subsearch.SubSearchActivity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchShoutsResultsActivity extends BaseActivity {

    public static final String KEY_SEARCH_QUERY = "query_to_save";
    public static final String KEY_CONTEXTUAL_ITEM_ID = "contextual_item_id";
    public static final String KEY_SEARCH_TYPE = "search_type";
    public static final String KEY_CATEGORY_NAME_FOR_SUB_SEARCH = "category_name";

    @Bind(R.id.search_shouts_results_toolbar)
    Toolbar toolbar;

    private SearchPresenter.SearchType searchType;
    private String contextualItemId;
    private String categoryName;

    public static Intent newIntent(@Nonnull Context context, @Nullable String queryToSave,
                                   @Nullable String contextualItemId,
                                   @Nonnull SearchPresenter.SearchType searchType) {
        return new Intent(context, SearchShoutsResultsActivity.class)
                .putExtra(KEY_SEARCH_QUERY, queryToSave)
                .putExtra(KEY_CONTEXTUAL_ITEM_ID, contextualItemId)
                .putExtra(KEY_SEARCH_TYPE, searchType);
    }

    public static Intent newIntent(@Nonnull Context context,
                                   @Nullable String queryToSave,
                                   @Nonnull String contextualItemId,
                                   @Nonnull SearchPresenter.SearchType searchType,
                                   @Nonnull String categoryName) {
        return new Intent(context, SearchShoutsResultsActivity.class)
                .putExtra(KEY_CATEGORY_NAME_FOR_SUB_SEARCH, categoryName)
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
        String searchQuery = intent.getStringExtra(KEY_SEARCH_QUERY);
        contextualItemId = intent.getStringExtra(KEY_CONTEXTUAL_ITEM_ID);
        searchType = (SearchPresenter.SearchType)
                checkNotNull(intent.getSerializableExtra(KEY_SEARCH_TYPE));
        categoryName = intent.getStringExtra(KEY_CATEGORY_NAME_FOR_SUB_SEARCH);

        final String toolbarTitle = searchQuery == null ? categoryName : searchQuery;
        setUpToolbar(toolbarTitle );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_search_results_container,
                            SearchShoutsResultsFragment.newInstance(searchQuery, contextualItemId, searchType))
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Let fragment handle results
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("PrivateResource")
    private void setUpToolbar(String title) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_shouts_results, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.search_results_menu_search:
                if (isFromSearchCategoriesOrTagProfile()) {
                    startActivity(SubSearchActivity.newIntent(
                            SearchShoutsResultsActivity.this, searchType,
                            contextualItemId, categoryName));
                } else {
                    startActivity(MainSearchActivity.newIntent(SearchShoutsResultsActivity.this));
                }
                return true;
            case R.id.search_results_menu_share:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isFromSearchCategoriesOrTagProfile() {
        return (searchType.equals(SearchPresenter.SearchType.CATEGORY) || searchType.equals(SearchPresenter.SearchType.TAG_PROFILE))
                && !TextUtils.isEmpty(categoryName);
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
