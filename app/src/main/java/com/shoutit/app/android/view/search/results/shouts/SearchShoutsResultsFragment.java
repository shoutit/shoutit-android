package com.shoutit.app.android.view.search.results.shouts;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragmentWithComponent;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.KeyboardHelper;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.view.filter.FiltersFragment;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.shout.ShoutActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchShoutsResultsFragment extends BaseFragmentWithComponent implements FiltersFragment.OnFiltersSubmitListener {

    @Bind(R.id.base_progress)
    View progressView;
    @Bind(R.id.search_shouts_results_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.search_results_drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.filter_drawer)
    View filterLayout;
    @Bind(R.id.search_results_header_title_tv)
    TextView headerTitleTv;
    @Bind(R.id.search_results_header_count_tv)
    TextView headerCountTv;
    @Bind(R.id.search_results_filter_iv)
    ImageView filterIv;
    @Bind(R.id.search_results_switch_iv)
    CheckedTextView layoutSwitchIcon;

    @Inject
    RecentSearchesTable recentSearchesTable;
    @Inject
    SearchShoutsResultsPresenter presenter;
    @Inject
    SearchShoutsResultsAdapter adapter;

    private ActionBarDrawerToggle drawerToggle;
    private SearchPresenter.SearchType searchType;
    private String searchQuery;
    private String contextualItemId;

    public static Fragment newInstance(@Nullable String searchQuery,
                                       @Nullable String contextualItemId,
                                       @Nonnull SearchPresenter.SearchType searchType) {
        final Bundle bundle = new Bundle();
        bundle.putString(SearchShoutsResultsActivity.KEY_SEARCH_QUERY, searchQuery);
        bundle.putString(SearchShoutsResultsActivity.KEY_CONTEXTUAL_ITEM_ID, contextualItemId);
        bundle.putSerializable(SearchShoutsResultsActivity.KEY_SEARCH_TYPE, searchType);

        final SearchShoutsResultsFragment fragment = new SearchShoutsResultsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_shoutsresults, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchType = (SearchPresenter.SearchType)
                checkNotNull(getArguments().getSerializable(SearchShoutsResultsActivity.KEY_SEARCH_TYPE));
        searchQuery = getArguments().getString(SearchShoutsResultsActivity.KEY_SEARCH_QUERY);
        contextualItemId = getArguments().getString(SearchShoutsResultsActivity.KEY_CONTEXTUAL_ITEM_ID);

        if (savedInstanceState == null && shouldShowFilters()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filter_drawer, FiltersFragment.newInstance(searchType, contextualItemId))
                    .commit();
        }

        final String searchQuery = getArguments().getString(SearchShoutsResultsActivity.KEY_SEARCH_QUERY);
        saveSearchQuery(searchQuery);
        setupHeader();
        initFiltersDrawer();
        initAdapter();

        presenter.getAdapterItems()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getProgressObservable()
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(RxView.visibility(progressView));

        presenter.getErrorObservable()
                .compose(this.<Throwable>bindToLifecycle())
                .subscribe(ColoredSnackBar.errorSnackBarAction(ColoredSnackBar.contentView(getActivity())));

        presenter.getShoutSelectedObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String shoutId) {
                        startActivity(ShoutActivity.newIntent(getActivity(), shoutId));
                    }
                });

        RxRecyclerView.scrollEvents(recyclerView)
                .compose(this.<RecyclerViewScrollEvent>bindToLifecycle())
                .filter(LoadMoreHelper.needLoadMore((MyLayoutManager) recyclerView.getLayoutManager(), adapter))
                .subscribe(presenter.getLoadMoreObserver());

        presenter.getCountObservable()
                .compose(this.<Integer>bindToLifecycle())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        headerCountTv.setText(getResources().getQuantityString(
                                R.plurals.shouts_results_pluaral, integer, integer));
                    }
                });

        filterIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldShowFilters()) {
                    drawerLayout.openDrawer(filterLayout);
                }
            }
        });

        layoutSwitchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutSwitchIcon.setChecked(!layoutSwitchIcon.isChecked());
                if (layoutSwitchIcon.isChecked()) {
                    layoutSwitchIcon.setBackground(getResources().getDrawable(R.drawable.ic_grid_switch));
                    setLinearLayoutManager();
                } else {
                    layoutSwitchIcon.setBackground(getResources().getDrawable(R.drawable.ic_list_switch));
                    setGridLayoutManager();
                }
            }
        });
    }

    private void setupHeader() {
        if (TextUtils.isEmpty(searchQuery)) {
            if (SearchPresenter.SearchType.BROWSE.equals(searchType)) {
                headerTitleTv.setText(getString(R.string.search_shout_results_header_title_location));
            } else {
                headerTitleTv.setText(getString(R.string.search_shout_results_all_results));
            }
        } else {
            headerTitleTv.setText(getString(R.string.search_shout_results_header_title, searchQuery));
        }
        filterIv.setVisibility(shouldShowFilters() ? View.VISIBLE : View.GONE);
    }

    private boolean shouldShowFilters() {
        return !(searchType.equals(SearchPresenter.SearchType.DISCOVER) ||
                searchType.equals(SearchPresenter.SearchType.PROFILE) ||
                searchType.equals(SearchPresenter.SearchType.RELATED_SHOUTS));
    }

    private void initAdapter() {
        recyclerView.addItemDecoration(new BaseShoutsItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.shouts_search_results_side_spacing)));
        setGridLayoutManager();
    }

    private void saveSearchQuery(@Nullable String query) {
        if (!TextUtils.isEmpty(query)) {
            recentSearchesTable.saveRecentSearch(query);
        }
    }

    private void setLinearLayoutManager() {
        LayoutManagerHelper.setLinearLayoutManager(getActivity(), recyclerView, adapter);
    }

    private void setGridLayoutManager() {
        LayoutManagerHelper.setGridLayoutManager(getActivity(), recyclerView, adapter);
    }

    private void initFiltersDrawer() {
        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        if (!shouldShowFilters()) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerClosed(View drawerView) {
                    KeyboardHelper.hideSoftKeyboard(getActivity());
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onFiltersSubmit(@Nonnull FiltersToSubmit filtersToSubmit) {
        presenter.getFiltersSelectedObserver().onNext(filtersToSubmit);
        drawerLayout.closeDrawers();
    }

    @Override
    protected BaseFragmentComponent injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                                    @Nonnull FragmentModule fragmentModule,
                                                    @Nullable Bundle savedInstanceState) {
        final Bundle bundle = checkNotNull(getArguments());
        final String searchQuery = bundle.getString(SearchShoutsResultsActivity.KEY_SEARCH_QUERY);
        final String contextualItemId = bundle.getString(SearchShoutsResultsActivity.KEY_CONTEXTUAL_ITEM_ID);
        final SearchPresenter.SearchType searchType =
                (SearchPresenter.SearchType) checkNotNull(bundle.getSerializable(SearchShoutsResultsActivity.KEY_SEARCH_TYPE));

        final SearchShoutsResultsFragmentComponent component = DaggerSearchShoutsResultsFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .searchShoutsResultsFragmentModule(new SearchShoutsResultsFragmentModule(searchQuery, contextualItemId, searchType))
                .build();

        component.inject(this);

        return component;
    }
}
