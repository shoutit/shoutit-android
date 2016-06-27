package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;

public class PageStats {

    @Nullable
    private final Integer totalUnreadCount;

    public PageStats(@Nullable Integer totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    @Nullable
    public Integer getTotalUnreadCount() {
        return totalUnreadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageStats)) return false;
        final PageStats pageStats = (PageStats) o;
        return Objects.equal(totalUnreadCount, pageStats.totalUnreadCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(totalUnreadCount);
    }
}
