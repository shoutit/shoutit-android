package com.shoutit.app.android.adapteritems;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public abstract class BaseNoIDAdapterItem implements BaseAdapterItem {
    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }
}
