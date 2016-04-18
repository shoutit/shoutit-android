package com.shoutit.app.android.view.chats.message_models;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DateItem dateItem = (DateItem) o;

        return date != null ? date.equals(dateItem.date) : dateItem.date == null;

    }

    @Override
    public int hashCode() {
        return date != null ? date.hashCode() : 0;
    }

    @Override
    public boolean matches(@Nonnull BaseAdapterItem item) {
        return item instanceof DateItem && item.equals(this);
    }

    @Override
    public boolean same(@Nonnull BaseAdapterItem item) {
        return item instanceof DateItem && item.equals(this);
    }
}
