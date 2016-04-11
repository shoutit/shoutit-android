package com.shoutit.app.android.api.model;

public class VerifyEmailResponse {
    private final String success;

    public VerifyEmailResponse(String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }
}
