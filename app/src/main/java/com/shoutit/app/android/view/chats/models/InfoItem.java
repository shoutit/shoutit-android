package com.shoutit.app.android.view.chats.models;

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
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return false;
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return false;
    }
}
