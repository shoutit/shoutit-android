package com.shoutit.app.android.viewholders;

import android.view.View;

import com.appunite.rx.android.adapter.ViewHolderManager;
import com.shoutit.app.android.adapteritems.NoDataAdapterItem;

import javax.annotation.Nonnull;

public class NoDataViewHolder extends ViewHolderManager.BaseViewHolder<NoDataAdapterItem> {

    public NoDataViewHolder(@Nonnull View itemView) {
        super(itemView);
    }

    @Override
    public void bind(@Nonnull NoDataAdapterItem item) {

    }
}
