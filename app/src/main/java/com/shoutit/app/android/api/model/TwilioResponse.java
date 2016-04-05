package com.shoutit.app.android.api.model;

public class TwilioResponse {

    private final String token;
    private final String identity;

    public TwilioResponse(String token, String identity) {

        this.token = token;
        this.identity = identity;
    }

    public String getToken() {
        return token;
    }

    public String getIdentity() {
        return identity;
    }
}
