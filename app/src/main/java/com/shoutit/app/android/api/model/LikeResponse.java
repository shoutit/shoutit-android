package com.shoutit.app.android.api.model;

public class LikeResponse {

    private final String success;

    public LikeResponse(final String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }
}
