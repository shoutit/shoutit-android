package com.shoutit.app.android.model;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class RelatedShoutsPointer {

    @Nonnull
    private final String shoutId;
    private final int pageSize;

    public RelatedShoutsPointer(@Nonnull String shoutId, int pageSize) {
        this.shoutId = shoutId;
        this.pageSize = pageSize;
    }

    @Nonnull
    public String getShoutId() {
        return shoutId;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelatedShoutsPointer)) return false;
        final RelatedShoutsPointer that = (RelatedShoutsPointer) o;
        return pageSize == that.pageSize &&
                Objects.equal(shoutId, that.shoutId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shoutId, pageSize);
    }
}
