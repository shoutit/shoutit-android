package com.shoutit.app.android.view.search.main.shouts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.search.SearchAdapter;
import com.shoutit.app.android.view.search.SearchPresenter;
import com.shoutit.app.android.view.search.main.MainSearchActivityComponent;
import com.shoutit.app.android.view.search.results.shouts.SearchShoutsResultsActivity;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.Bind;
import rx.functions.Action1;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchShoutFragment extends BaseFragment {

    private static final String KEY_SEARCH_TYPE = "search_type";

    @Bind(R.id.search_recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.base_progress)
    View progressView;

    @Inject
    SearchWithRecentsPresenter presenter;
    @Inject
    SearchAdapter adapter;

    public static Fragment newInstance(@Nonnull SearchPresenter.SearchType searchType) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_SEARCH_TYPE, searchType);

        final SearchShoutFragment fragment = new SearchShoutFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        presenter.getRecentsSearchesObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);

        presenter.getClearAllRecentsObservable()
                .compose(bindToLifecycle())
                .subscribe();

        presenter.getRemoveRecentObservable()
                .compose(this.bindToLifecycle())
                .subscribe();

        presenter.getRecentSearchClickObservable()
                .compose(this.<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String recentSearch) {
                        startActivity(SearchShoutsResultsActivity.newIntent(getActivity(), recentSearch));
                    }
                });

        // TODO uncomment when API adjust changes
/*        presenter.getSearchPresenter()
                .getSuggestionsAdapterItemsObservable()
                .compose(this.<List<BaseAdapterItem>>bindToLifecycle())
                .subscribe(adapter);*/
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        final Bundle bundle = checkNotNull(getArguments());
        final SearchPresenter.SearchType searchType = (SearchPresenter.SearchType) bundle.getSerializable(KEY_SEARCH_TYPE);

        DaggerSearchShoutFragmentComponent.builder()
                .mainSearchActivityComponent((MainSearchActivityComponent) baseActivityComponent)
                .fragmentModule(fragmentModule)
                .searchShoutsFragmentModule(new SearchShoutsFragmentModule(this, searchType))
                .build()
                .inject(this);
    }
}
