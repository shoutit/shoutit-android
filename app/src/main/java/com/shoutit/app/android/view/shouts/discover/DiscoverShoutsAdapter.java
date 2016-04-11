package com.shoutit.app.android.view.shouts.discover;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinerViewHolder;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class DiscoverShoutsAdapter extends ChangeableLayoutManagerAdapter {

    private final Picasso mPicasso;

    @Inject
    public DiscoverShoutsAdapter(@ForActivity Context context, Picasso picasso) {
        super(context);
        mPicasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return isLinearLayoutManager ?
                new ShoutLinerViewHolder(layoutInflater.inflate(R.layout.home_feed_item_linear, parent, false), context, mPicasso) :
                new ShoutGridViewHolder(layoutInflater.inflate(R.layout.shout_item_grid, parent, false), mPicasso);
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        //noinspection unchecked
        holder.bind(items.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_SHOUT;
    }
}
