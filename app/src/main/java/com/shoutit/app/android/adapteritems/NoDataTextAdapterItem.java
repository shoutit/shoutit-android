package com.shoutit.app.android.adapteritems;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class NoDataTextAdapterItem extends BaseNoIDAdapterItem {

    @Nonnull
    private final String textToDisplay;

    public NoDataTextAdapterItem(@Nonnull String textToDisplay) {
        this.textToDisplay = textToDisplay;
    }

    @Nonnull
    public String getTextToDisplay() {
        return textToDisplay;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof NoDataTextAdapterItem;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof NoDataTextAdapterItem
                && textToDisplay.equals(((NoDataTextAdapterItem) item).getTextToDisplay());
    }
}

