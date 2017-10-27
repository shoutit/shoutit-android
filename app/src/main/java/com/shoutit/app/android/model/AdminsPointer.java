package com.shoutit.app.android.model;

import com.google.common.base.Objects;

public class AdminsPointer {
    private final String userName;
    private final int pageSize;

    public AdminsPointer(String userName, int pageSize) {
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
        if (!(o instanceof AdminsPointer)) return false;
        final AdminsPointer that = (AdminsPointer) o;
        return pageSize == that.pageSize &&
                Objects.equal(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName, pageSize);
    }
}