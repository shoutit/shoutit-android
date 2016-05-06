package com.shoutit.app.android.api.model;

public class VideoCallRequest {

    private final String identity;
    private final boolean missed;

    public VideoCallRequest(String identity, boolean missed) {
        this.identity = identity;
        this.missed = missed;
    }
}
