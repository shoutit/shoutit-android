package com.shoutit.app.android.model;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class TagShoutsPointer {

    private final int pageSize;
    @Nonnull
    private final String tagName;
    @Nonnull
    private final LocationPointer locationPointer;

    public TagShoutsPointer(int pageSize, @Nonnull String tagName, @Nonnull LocationPointer locationPointer) {
        this.pageSize = pageSize;
        this.tagName = tagName;
        this.locationPointer = locationPointer;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Nonnull
    public String getTagName() {
        return tagName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagShoutsPointer)) return false;
        TagShoutsPointer that = (TagShoutsPointer) o;
        return pageSize == that.pageSize &&
                Objects.equal(tagName, that.tagName) &&
                Objects.equal(locationPointer, that.locationPointer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pageSize, tagName, locationPointer);
    }
}

