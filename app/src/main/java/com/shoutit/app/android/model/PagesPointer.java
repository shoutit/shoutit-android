package com.shoutit.app.android.model;

import com.google.common.base.Objects;

public class PagesPointer {
    private final String userName;
    private final int pageSize;

    public PagesPointer(String userName, int pageSize) {
        this.userName = userName;
        this.pageSize = pageSize;
    }

    public String getUserName() {
        return userName;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagesPointer)) return false;
        final PagesPointer that = (PagesPointer) o;
        return pageSize == that.pageSize &&
                Objects.equal(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName, pageSize);
    }
}