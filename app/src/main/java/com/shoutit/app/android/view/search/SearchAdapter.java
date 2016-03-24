package com.shoutit.app.android.view.search;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.search.main.shouts.SearchWithRecentsPresenter;
import com.shoutit.app.android.viewholders.NoDataViewHolder;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_RECENT_SEARCHES_HEADER = 1;
    private static final int VIEW_TYPE_RECENT_SEARCH = 2;
    private static final int VIEW_TYPE_SUGGESTION = 3;
    private static final int VIEW_TYPE_SHADOW = 4;

    @Inject
    public SearchAdapter(@ForActivity @Nonnull Context context) {
        super(context);
    }

    public class RecentsHeaderViewHolder extends ViewHolderManager.BaseViewHolder<SearchWithRecentsPresenter.RecentsHeaderAdapterItem> {

        private SearchWithRecentsPresenter.RecentsHeaderAdapterItem item;

        public RecentsHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull SearchWithRecentsPresenter.RecentsHeaderAdapterItem item) {
            this.item = item;
        }

        @OnClick(R.id.search_clear_all_tv)
        public void onClearAllClicked() {
            item.onClearAllRecentsClicked();
        }
    }

    public class RecentSearchViewHolder extends ViewHolderManager.BaseViewHolder<SearchWithRecentsPresenter.RecentSearchAdapterItem> implements View.OnClickListener {

        @Bind(R.id.search_recent_text_tv)
        TextView recentSearchTv;

        private SearchWithRecentsPresenter.RecentSearchAdapterItem item;

        public RecentSearchViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(@Nonnull SearchWithRecentsPresenter.RecentSearchAdapterItem item) {
            this.item = item;
            recentSearchTv.setText(item.getSuggestion());
        }

        @OnClick(R.id.search_clear_recent_iv)
        public void onSuggestionRemove() {
            item.onSuggestionRemove();
        }

        @Override
        public void onClick(View v) {
            item.onRecentSearchClicked();
        }
    }

    public class SuggestionViewHolder extends ViewHolderManager.BaseViewHolder<SearchPresenter.SearchSuggestionAdapterItem> {

        @Bind(R.id.search_suggestion_tv)
        TextView suggesitonTv;

        private SearchPresenter.SearchSuggestionAdapterItem item;

        public SuggestionViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull SearchPresenter.SearchSuggestionAdapterItem item) {
            this.item = item;
            suggesitonTv.setText(item.getSuggestionText());
        }

        @OnClick(R.id.search_fill_search_iv)
        public void onFillSearchClicked() {
            item.onFillSearchWithSuggestion();
        }

        @OnClick(R.id.search_item_selector)
        public void onSuggestionClicked() {
            item.onSuggestionClick();
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_RECENT_SEARCHES_HEADER:
                return new RecentsHeaderViewHolder(layoutInflater.inflate(R.layout.search_recent_header_item, parent, false));
            case VIEW_TYPE_RECENT_SEARCH:
                return new RecentSearchViewHolder(layoutInflater.inflate(R.layout.search_recent_item, parent, false));
            case VIEW_TYPE_SUGGESTION:
                return new SuggestionViewHolder(layoutInflater.inflate(R.layout.search_item, parent, false));
            case VIEW_TYPE_SHADOW:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.shadow_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type:" + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof SearchWithRecentsPresenter.RecentsHeaderAdapterItem) {
            return VIEW_TYPE_RECENT_SEARCHES_HEADER;
        } else if (item instanceof SearchWithRecentsPresenter.RecentSearchAdapterItem) {
            return VIEW_TYPE_RECENT_SEARCH;
        } else if (item instanceof SearchPresenter.SearchSuggestionAdapterItem) {
            return VIEW_TYPE_SUGGESTION;
        } else if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_SHADOW;
        } else {
            throw new RuntimeException("Unknown view type for:" + item.getClass().getSimpleName());
        }
    }
}
