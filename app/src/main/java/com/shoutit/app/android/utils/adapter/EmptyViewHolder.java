package com.shoutit.app.android.utils.adapter;

import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;

import javax.annotation.Nonnull;

public class EmptyViewHolder extends ViewHolderManager.BaseViewHolder<BaseAdapterItem> {

    public EmptyViewHolder(@Nonnull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@Nonnull BaseAdapterItem t) {

    }
}
