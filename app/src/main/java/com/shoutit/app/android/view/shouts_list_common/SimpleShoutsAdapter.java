package com.shoutit.app.android.view.shouts_list_common;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.R;
import com.shoutit.app.android.adapteritems.NoDataTextAdapterItem;
import com.shoutit.app.android.adapters.ChangeableLayoutManagerAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.shoutit.app.android.view.shouts.ShoutAdapterItem;
import com.shoutit.app.android.view.shouts.ShoutGridViewHolder;
import com.shoutit.app.android.view.shouts.ShoutLinearViewHolder;
import com.shoutit.app.android.viewholders.NoDataTextViewHolder;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

public class SimpleShoutsAdapter extends ChangeableLayoutManagerAdapter {
    public static final int VIEW_TYPE_NO_RESULTS = 3;

    private final Picasso picasso;
    private final Picasso picassoNoTransformer;

    @Inject
    public SimpleShoutsAdapter(@ForActivity @Nonnull Context context,
                               Picasso picasso,
                               @Named("NoAmazonTransformer") Picasso picassoNoTransformer) {
        super(context);
        this.picasso = picasso;
        this.picassoNoTransformer = picassoNoTransformer;
    }


    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHOUT:
                return isLinearLayoutManager ?
                        new ShoutLinearViewHolder(layoutInflater.inflate(R.layout.shout_item_linear, parent, false), context, picasso, picassoNoTransformer) :
                        new ShoutGridViewHolder(layoutInflater.inflate(ShoutGridViewHolder.getLayoutRes(), parent, false), picasso);
            case VIEW_TYPE_NO_RESULTS:
                return new NoDataTextViewHolder(layoutInflater.inflate(R.layout.no_data_text_adapter_item, parent, false));
            default:
                throw new RuntimeException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
        if (item instanceof ShoutAdapterItem) {
            return VIEW_TYPE_SHOUT;
        } else if (item instanceof NoDataTextAdapterItem) {
            return VIEW_TYPE_NO_RESULTS;
        } else {
            throw new RuntimeException("Unknown view type: " + item.getClass().getSimpleName());
        }
    }
}