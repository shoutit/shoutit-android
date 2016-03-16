package com.shoutit.app.android.view.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shoutit.app.android.BaseFragment;
import com.shoutit.app.android.dagger.BaseActivityComponent;
import com.shoutit.app.android.dagger.FragmentModule;
import com.shoutit.app.android.view.search.shouts.DaggerSearchShoutFragmentComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.appunite.rx.internal.Preconditions.checkNotNull;

public class SearchFragment extends BaseFragment {

    private static final String KEY_SEARCH_TYPE = "search_type";
    private static final String KEY_SHOW_SUGGESTIONS = "show_suggestions";
    private static final String KEY_CONTEXTUAL_ITEM_ID = "contextual_item_id";

    @Inject
    SearchPresenter searchPresenter;

    public static Fragment newInstance(@Nonnull SearchPresenter.SearchType searchType, boolean showSuggestions) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_SEARCH_TYPE, searchType);
        bundle.putBoolean(KEY_SHOW_SUGGESTIONS, showSuggestions);

        final SearchFragment fragment = new SearchFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static Fragment newInstance(@Nonnull SearchPresenter.SearchType searchType,
                                       boolean showSuggestions,
                                       @Nonnull String contextualItemId) {
        final Fragment fragment = newInstance(searchType, showSuggestions);
        fragment.getArguments().putString(KEY_CONTEXTUAL_ITEM_ID, contextualItemId);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void injectComponent(@Nonnull BaseActivityComponent baseActivityComponent,
                                   @Nonnull FragmentModule fragmentModule,
                                   @Nullable Bundle savedInstanceState) {
        final Bundle bundle = checkNotNull(getArguments());
        final SearchPresenter.SearchType searchType = (SearchPresenter.SearchType) bundle.getSerializable(KEY_SEARCH_TYPE);
        final boolean showSuggestions = bundle.getBoolean(KEY_SHOW_SUGGESTIONS);
        final String contextualItemId = bundle.getString(KEY_CONTEXTUAL_ITEM_ID);

        DaggerSearchFragmentComponent.builder()
                .baseActivityComponent(baseActivityComponent)
                .fragmentModule(fragmentModule)
                .searchFragmentModule(new SearchFragmentModule(this, searchType, showSuggestions, contextualItemId))
                .build()
                .inject(this);
    }
}
