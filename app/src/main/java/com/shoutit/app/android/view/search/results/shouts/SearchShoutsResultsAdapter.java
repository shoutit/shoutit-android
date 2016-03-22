package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.jakewharton.rxbinding.view.RxView;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinerViewHolder;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class SearchShoutsResultsAdapter extends BaseAdapter {
    public static final int VIEW_TYPE_SHOUT_HEADER = 1;
    public static final int VIEW_TYPE_SHOUT = 2;
    public static final int VIEW_TYPE_NO_RESULTS = 3;

    private final Picasso picasso;
    private boolean isLinearLayoutManager = true;

    @Inject
    public SearchShoutsResultsAdapter(@ForActivity @Nonnull Context context, Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    public void switchLayoutManager(boolean isLinearLayoutManager) {
        this.isLinearLayoutManager = isLinearLayoutManager;
        notifyDataSetChanged();
    }

    public class SearchShoutHeaderViewHolder extends ViewHolderManager.BaseViewHolder<SearchShoutsResultsPresenter.ShoutHeaderAdapterItem> {

        @Bind(R.id.search_results_header_title_tv)
        TextView headerTitleTv;
        @Bind(R.id.search_results_header_count_tv)
        TextView headerCountTv;
        @Bind(R.id.search_results_filter_iv)
        ImageView filterIv;
        @Bind(R.id.search_results_switch_iv)
        ImageView layoutSwitchIv;

        private SearchShoutsResultsPresenter.ShoutHeaderAdapterItem item;
        private CompositeSubscription subscription;

        public SearchShoutHeaderViewHolder(@Nonnull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(@Nonnull SearchShoutsResultsPresenter.ShoutHeaderAdapterItem item) {
            this.item = item;

            headerTitleTv.setText(context.getString(R.string.search_shout_results_header_title, item.getSearchQuery()));
            headerCountTv.setText(context.getString(R.string.search_shouts_results_header_count, item.getTotalItemsCount()));

            layoutSwitchIv.setImageDrawable(context.getResources().getDrawable(
                    isLinearLayoutManager ? R.drawable.ic_grid_switch : R.drawable.ic_list_switch));

            subscription = new CompositeSubscription(
                    RxView.clicks(layoutSwitchIv)
                            .subscribe(item.getLayoutManagerSwitchObserver()),

                    RxView.clicks(filterIv)
                            .subscribe(new Action1<Void>() {
                                @Override
                                public void call(Void aVoid) {
                                    Toast.makeText(context, "Not implemented yet", Toast.LENGTH_LONG).show();
                                }
                            })
            );
        }

        @Override
        public void onViewRecycled() {
            recycle();
            super.onViewRecycled();
        }

        private void recycle() {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHOUT_HEADER:
                return new SearchShoutHeaderViewHolder(layoutInflater.inflate(R.layout.search_shout_results_header_item, parent, false));
            case VIEW_TYPE_SHOUT:
                return isLinearLayoutManager ?
                        new ShoutLinerViewHolder(layoutInflater.inflate(R.layout.home_feed_item_linear, parent, false), context, picasso) :
                        new ShoutGridViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false), picasso);
            case VIEW_TYPE_NO_RESULTS:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.search_shouts_results_no_results, parent, false));
            default:
                throw new RuntimeException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof SearchShoutsResultsPresenter.ShoutHeaderAdapterItem) {
            return VIEW_TYPE_SHOUT_HEADER;
        } else if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_NO_RESULTS;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
