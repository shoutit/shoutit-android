package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class UpdateFacebookTokenRequest {

    @Nonnull
    private final String account = "facebook";
    @Nonnull
    private final String facebookAccessToken;

    public UpdateFacebookTokenRequest(@Nonnull String facebookAccessToken) {
        this.facebookAccessToken = facebookAccessToken;
    }

    @Nonnull
    public String getFacebookAccessToken() {
        return facebookAccessToken;
    }
}
