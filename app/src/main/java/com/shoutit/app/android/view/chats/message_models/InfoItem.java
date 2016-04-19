package com.shoutit.app.android.view.chats.message_models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class InfoItem implements BaseAdapterItem {

    private final String info;

    public InfoItem(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public long adapterId() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InfoItem infoItem = (InfoItem) o;

        return info != null ? info.equals(infoItem.info) : infoItem.info == null;
    }

    @Override
    public int hashCode() {
        return info != null ? info.hashCode() : 0;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof InfoItem && this.equals(item);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof InfoItem && this.equals(item);
    }
}
