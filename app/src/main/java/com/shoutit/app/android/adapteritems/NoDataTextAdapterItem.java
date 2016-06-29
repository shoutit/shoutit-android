package com.shoutit.app.android.adapteritems;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoDataTextAdapterItem extends BaseNoIDAdapterItem {

    @Nullable
    private final String textToDisplay;

    public NoDataTextAdapterItem(@Nullable String textToDisplay) {
        this.textToDisplay = textToDisplay;
    }

    @Nullable
    public String getTextToDisplay() {
        return textToDisplay;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof NoDataTextAdapterItem;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof NoDataTextAdapterItem;
    }
}

