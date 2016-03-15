package com.shoutit.app.android.adapteritems;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class NoDataAdapterItem extends BaseNoIDAdapterItem {

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return false;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return false;
    }
}
