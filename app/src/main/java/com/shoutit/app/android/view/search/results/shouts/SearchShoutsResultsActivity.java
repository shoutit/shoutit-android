package com.shoutit.app.android.view.search.results.shouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxToolbar;
import com.shoutit.app.android.App;
import com.shoutit.app.android.BaseActivity;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ActivityModule;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyGridLayoutManager;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.utils.MyLinearLayoutManager;
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

    public static Intent newIntent(Context context, @Nonnull String queryToSave) {
        return new Intent(context, SearchShoutsResultsActivity.class)
                .putExtra(KEY_SEARCH_QUERY, queryToSave);
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
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            final int sideSpacing = getResources().getDimensionPixelSize(R.dimen.shouts_search_results_side_spacing);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

                int position = parent.getChildAdapterPosition(view);

                final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }

                if (layoutManager instanceof MyLinearLayoutManager ||
                        (layoutManager instanceof MyGridLayoutManager && ((GridLayoutManager) layoutManager).getSpanSizeLookup().getSpanSize(position) == 2)) {
                    outRect.left = sideSpacing;
                    outRect.right = sideSpacing;
                } else {
                    if (position % 2 == 0) {
                        outRect.right = sideSpacing;
                    } else {
                        outRect.left = sideSpacing;
                    }
                }
            }
        });
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
        recyclerView.setLayoutManager(new MyLinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(true);
    }

    private void setGridLayoutManager() {
        final MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItemViewType(position) == SearchShoutsResultsAdapter.VIEW_TYPE_SHOUT) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.switchLayoutManager(false);
    }

    @Nonnull
    @Override
    public BaseActivityComponent createActivityComponent(@Nullable Bundle savedInstanceState) {
        final String searchQuery = checkNotNull(getIntent().getStringExtra(KEY_SEARCH_QUERY));

        final SearchShoutsResultsActivityComponent component = DaggerSearchShoutsResultsActivityComponent
                .builder()
                .activityModule(new ActivityModule(this))
                .searchShoutsResultsActivityModule(new SearchShoutsResultsActivityModule(searchQuery))
                .appComponent(App.getAppComponent(getApplication()))
                .build();
        component.inject(this);

        return component;
    }
}
