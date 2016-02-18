package com.shoutit.app.android.view.shouts;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.R;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class ShoutsAdapter extends BaseAdapter {

    private final Picasso mPicasso;
    private boolean isLinearLayoutManager = false;

    @Inject
    public ShoutsAdapter(@ForActivity Context context, Picasso picasso) {
        super(context);
        mPicasso = picasso;
    }

    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return isLinearLayoutManager ?
                new ShoutLinerViewHolder(layoutInflater.inflate(R.layout.home_feed_item_linear, parent, false), context, mPicasso) :
                new ShoutGridViewHolder(layoutInflater.inflate(R.layout.home_feed_item_grid, parent, false), mPicasso, context);
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {
        //noinspection unchecked
        holder.bind(items.get(position));
    }

    public void switchLayoutManager(boolean isLinearLayoutManager) {
        this.isLinearLayoutManager = isLinearLayoutManager;
        notifyDataSetChanged();
    }
}
