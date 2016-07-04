package com.shoutit.app.android.view.shouts.discover;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.FbAdAdapterItem;
import com.shoutit.app.android.adapters.FBAdsAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinerViewHolder;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;
import javax.inject.Named;

public class DiscoverShoutsAdapter extends FBAdsAdapter {

    private final Picasso mPicasso;
    private final Picasso picassoNoTransformer;

    @Inject
    public DiscoverShoutsAdapter(@ForActivity Context context,
                                 Picasso picasso,
                                 @Named("NoAmazonTransformer") Picasso picassoNoTransformer) {
        super(context);
        mPicasso = picasso;
        this.picassoNoTransformer = picassoNoTransformer;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AD) {
            return super.onCreateViewHolder(parent, viewType);
        } else {
            return isLinearLayoutManager ?
                    new ShoutLinerViewHolder(layoutInflater.inflate(R.layout.home_feed_item_linear, parent, false), context, mPicasso, picassoNoTransformer) :
                    new ShoutGridViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false), mPicasso);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem adapterItem = items.get(position);
        if (adapterItem instanceof FbAdAdapterItem) {
            return super.getItemViewType(position);
        } else {
            return VIEW_TYPE_SHOUT;
        }
    }
}
