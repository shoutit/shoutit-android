package com.shoutit.app.android.view.chats.models;

import com.appunite.rx.android.adapter.BaseAdapterItem;

import javax.annotation.Nonnull;

public class DateItem implements BaseAdapterItem {

    private final String date;

    public DateItem(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
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
