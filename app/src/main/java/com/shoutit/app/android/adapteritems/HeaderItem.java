package com.shoutit.app.android.adapteritems;

import android.support.annotation.Nullable;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class HeaderItem extends BaseNoIDAdapterItem {

    @Nullable
    private final String headerText;

    public HeaderItem(@Nullable String headerText) {
        this.headerText = headerText;
    }

    @Nullable
    public String getHeaderText() {
        return headerText;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof HeaderItem;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof HeaderItem;
    }
}