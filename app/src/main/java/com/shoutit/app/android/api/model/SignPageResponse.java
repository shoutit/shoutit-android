package com.shoutit.app.android.api.model;


public class SignPageResponse {

    private final String accessToken;
    private final String tokenType;
    private final String refreshToken;
    private final boolean newSignup;
    private final Page profile;

    public SignPageResponse(String accessToken, String tokenType, String refreshToken, boolean newSignup, Page profile) {
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

    public Page getProfile() {
        return profile;
    }
}
