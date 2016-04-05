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

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.BaseFragmentWithComponent;
import com.shoutit.app.android.BaseShoutsItemDecoration;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.BaseFragmentComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.db.RecentSearchesTable;
import com.shoutit.app.android.model.FiltersToSubmit;
import com.shoutit.app.android.utils.ColoredSnackBar;
import com.shoutit.app.android.utils.LayoutManagerHelper;
import com.shoutit.app.android.utils.LoadMoreHelper;
import com.shoutit.app.android.utils.MyLayoutManager;
import com.shoutit.app.android.view.filter.FiltersFragment;
import com.shoutit.app.android.view.filter.FiltersPresenter;
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

    @Inject
    RecentSearchesTable recentSearchesTable;
    @Inject
    SearchShoutsResultsPresenter presenter;
    @Inject
    SearchShoutsResultsAdapter adapter;

    private ActionBarDrawerToggle drawerToggle;

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

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filter_drawer, FiltersFragment.newInstance())
                    .commit();
        }

        final String searchQuery = getArguments().getString(SearchShoutsResultsActivity.KEY_SEARCH_QUERY);
        saveSearchQuery(searchQuery);
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

        presenter.getFiltersOpenObservable()
                .compose(bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        drawerLayout.openDrawer(filterLayout);
                    }
                });
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
