package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

public class PaginatedResponse {

    private final int count;
    @Nullable
    private final String next;
    @Nullable
    private final String previous;

    public PaginatedResponse(int count, @Nullable String next, @Nullable String previous) {
        this.count = count;
        this.next = next;
        this.previous = previous;
    }

    public int getCount() {
        return count;
    }

    @Nullable
    public String getNext() {
        return next;
    }

    @Nullable
    public String getPrevious() {
        return previous;
    }
}
