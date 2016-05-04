package com.shoutit.app.android.api.model;

public class TwilioResponse {

    private final String token;
    private final String identity;
    private final long expiresAt;

    public TwilioResponse(String token, String identity, long expiresAt) {
        this.token = token;
        this.identity = identity;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public String getIdentity() {
        return identity;
    }
}
