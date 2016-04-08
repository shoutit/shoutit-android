package com.shoutit.app.android.view.search.results.shouts;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinerViewHolder;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class SearchShoutsResultsAdapter extends ChangeableLayoutManagerAdapter {
    public static final int VIEW_TYPE_NO_RESULTS = 3;

    private final Picasso picasso;

    @Inject
    public SearchShoutsResultsAdapter(@ForActivity @Nonnull Context context, Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }


    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
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
        if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof NoDataAdapterItem) {
            return VIEW_TYPE_NO_RESULTS;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}
