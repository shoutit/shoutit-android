package com.shoutit.app.android.api.model;

public class SignResponse {

    private final String accessToken;
    private final String tokenType;
    private final String refreshToken;
    private final boolean newSingup;

    public SignResponse(String accessToken, String tokenType, String refreshToken, boolean newSingup) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.refreshToken = refreshToken;
        this.newSingup = newSingup;
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

    public boolean isNewSingup() {
        return newSingup;
    }
}
