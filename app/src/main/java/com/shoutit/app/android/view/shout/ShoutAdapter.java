package com.shoutit.app.android.view.shout;

import android.content.Context;
import android.view.ViewGroup;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.BaseAdapter;
import com.shoutit.app.android.dagger.ForActivity;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ShoutAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_SHOUT = 1;
    private static final int VIEW_TYPE_USER_SHOUTS = 2;
    private static final int VIEW_TYPE_VISIW_PROFILE = 3;
    private static final int VIEW_TYPE_RELATED_SHOUTS = 4;

    @Nonnull
    private final Picasso picasso;

    @Inject
    public ShoutAdapter(@ForActivity @Nonnull Context context, @Nonnull Picasso picasso) {
        super(context);
        this.picasso = picasso;
    }



    @Override
    public ViewHolderManager.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolderManager.BaseViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        final BaseAdapterItem item = items.get(position);
    }
}
