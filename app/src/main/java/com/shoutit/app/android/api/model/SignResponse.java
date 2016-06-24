package com.shoutit.app.android.api.model;


public class SignResponse {

    private final String accessToken;
    private final String tokenType;
    private final String refreshToken;
    private final boolean newSignup;
    private final User profile;

    public SignResponse(String accessToken, String tokenType, String refreshToken, boolean newSignup, User profile) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
        this.newSignup = newSignup;
        this.profile = profile;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean isNewSignup() {
        return newSignup;
    }

    public User getProfile() {
        return profile;
    }
}
