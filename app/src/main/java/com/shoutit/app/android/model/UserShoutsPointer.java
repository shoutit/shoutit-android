package com.shoutit.app.android.model;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;

public class UserShoutsPointer {

    private final int pageSize;
    @Nonnull
    private final String userName;

    public UserShoutsPointer(int pageSize, @Nonnull String userName) {
        this.pageSize = pageSize;
        this.userName = userName;
    }

    public int getPageSize() {
        return pageSize;
    }

    @Nonnull
    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserShoutsPointer)) return false;
        UserShoutsPointer that = (UserShoutsPointer) o;
        return pageSize == that.pageSize &&
                Objects.equal(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pageSize, userName);
    }
}
