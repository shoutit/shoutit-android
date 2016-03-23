package com.shoutit.app.android.view.search.results.shouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.shout.ShoutActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchShoutsResultsActivity extends BaseActivity {

    private static final String KEY_SEARCH_QUERY = "query_to_save";
    private static final String KEY_CONTEXTUAL_ITEM_ID = "contextual_item_id";
    private static final String KEY_SEARCH_TYPE = "search_type";

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.search_shouts_results_toolbar)
    Toolbar toolbar;
    @Bind(R.id.search_shouts_results_recycler_view)
    RecyclerView recyclerView;

    @Inject
    RecentSearchesTable recentSearchesTable;
    @Inject
    SearchShoutsResultsPresenter presenter;
    @Inject
    SearchShoutsResultsAdapter adapter;

    public static Intent newIntent(Context context, @Nonnull String queryToSave,
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

        setUpToolbar();

        if (savedInstanceState == null) {
            saveSearchQuery();
        }

        initAdapter();

        presenter.getAdapterItems()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(this)));

        presenter.getLinearLayoutManagerObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ignore) {
                        setLinearLayoutManager();
                    }
                });

        presenter.getGridLayoutManagerObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean ignore) {
                        setGridLayoutManager();
                    }
                });

        presenter.getToolbarTitleObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String toolbarTitle) {
                        toolbar.setTitle(toolbarTitle);
                    }
                });

        presenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(SearchShoutsResultsActivity.this, shoutId));
                    }
                });

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());
    }

    private void initAdapter() {
        recyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.shouts_search_results_side_spacing)));
        setGridLayoutManager();
    }

    @SuppressLint("PrivateResource")
    private void setUpToolbar() {
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

    private void saveSearchQuery() {
        final Intent intent = checkNotNull(getIntent());
        final String query = intent.getStringExtra(KEY_SEARCH_QUERY);
        if (!TextUtils.isEmpty(query)) {
            recentSearchesTable.saveRecentSearch(query);
        }
    }

    private void setLinearLayoutManager() {
        LayoutManagerHelper.setLinearLayoutManager(this, recyclerView, adapter);
    }

    private void setGridLayoutManager() {
        LayoutManagerHelper.setGridLayoutManager(this, recyclerView, adapter);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final Intent intent = checkNotNull(getIntent());
        final String searchQuery = checkNotNull(intent.getStringExtra(KEY_SEARCH_QUERY));
        final String contextualItemId = intent.getStringExtra(KEY_CONTEXTUAL_ITEM_ID);
        final SearchPresenter.SearchType searchType = (SearchPresenter.SearchType) checkNotNull(intent.getSerializableExtra(KEY_SEARCH_TYPE));

        final SearchShoutsResultsActivityComponent component = DaggerSearchShoutsResultsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .searchShoutsResultsActivityModule(new SearchShoutsResultsActivityModule(searchQuery, contextualItemId, searchType))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
