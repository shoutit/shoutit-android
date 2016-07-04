package com.shoutit.app.android.view.shouts.discover;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinearViewHolder;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;
import javax.inject.Named;

public class DiscoverShoutsAdapter extends ChangeableLayoutManagerAdapter {

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
        return isLinearLayoutManager ?
                new ShoutLinearViewHolder(layoutInflater.inflate(R.layout.shout_item_linear, parent, false), context, mPicasso, picassoNoTransformer) :
                new ShoutGridViewHolder(layoutInflater.inflate(ShoutGridViewHolder.getLayoutRes(), parent, false), mPicasso);
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
