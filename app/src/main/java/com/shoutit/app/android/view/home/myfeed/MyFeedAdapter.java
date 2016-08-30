package com.shoutit.app.android.view.home.myfeed;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.BaseShoutAdapterItem;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;
import com.shoutit.app.android.adapters.FBAdsAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.viewholders.NoDataViewHolder;
import com.shoutit.app.android.viewholders.ShoutViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class MyFeedAdapter extends FBAdsAdapter {

    private static final int VIEW_TYPE_EMPTY_FEED = 2;

    private final Picasso picasso;

    @Inject
    public MyFeedAdapter(@ForActivity @Nonnull Context context,
                         Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHOUT:
                return new ShoutViewHolder(layoutInflater.inflate(ShoutGridViewHolder.getLayoutRes(), parent, false), picasso);
            case VIEW_TYPE_EMPTY_FEED:
                return new NoDataViewHolder(layoutInflater.inflate(R.layout.empty_feed, parent, false));
            default:
                throw new RuntimeException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem baseAdapterItem = items.get(position);
        if (baseAdapterItem instanceof BaseShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (baseAdapterItem instanceof NoDataAdapterItem) {
            return VIEW_TYPE_EMPTY_FEED;
        } else {
            throw new RuntimeException("Unknown view type: " + baseAdapterItem.getClass().getSimpleName());
        }
    }
}
