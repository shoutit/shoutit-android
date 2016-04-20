package com.shoutit.app.android.api.model;

public class TwillioRejectCallRequest {

    private final String identity;
    private final boolean missed;

    public TwillioRejectCallRequest(String identity, boolean missed) {
        this.identity = identity;
        this.missed = missed;
    }
}
