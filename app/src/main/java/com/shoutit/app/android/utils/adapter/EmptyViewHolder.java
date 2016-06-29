package com.shoutit.app.android.utils.adapter;

import android.view.View;

import com.appunite.rx.android.adapter.BaseAdapterItem;
import com.appunite.rx.android.adapter.ViewHolderManager;

import javax.annotation.Nonnull;

public class EmptyViewHolder<T extends BaseAdapterItem> extends ViewHolderManager.BaseViewHolder<T> {

    public EmptyViewHolder(@Nonnull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@Nonnull T t) {

    }
}
