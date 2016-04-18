package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import java.util.List;

public class ShoutsResponse extends PaginatedResponse {

    @NonNull
    private final List<Shout> results;
    @NonNull
    private final String webUrl;

    public ShoutsResponse(int count, String next, String previous, @NonNull List<Shout> results, @NonNull String webUrl) {
        super(count, next, previous);
        this.results = results;
        this.webUrl = webUrl;
    }

    @NonNull
    public List<Shout> getShouts() {
        return results;
    }

    @NonNull
    public String getWebUrl() {
        return webUrl;
    }
}
