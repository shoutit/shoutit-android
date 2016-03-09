package com.shoutit.app.android.adapteritems;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class HeaderAdapterItem implements BaseAdapterItem {

    @Nonnull
    private final String title;

    public HeaderAdapterItem(@Nonnull String title) {
        this.title = title;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }


    @Override
    public long adapterId() {
        return BaseAdapterItem.NO_ID;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof HeaderAdapterItem;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof HeaderAdapterItem && title.equals(((HeaderAdapterItem) item).getTitle());
    }
}