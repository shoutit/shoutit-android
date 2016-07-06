package com.shoutit.app.android.api.model;

public class LinkFacebookRequest {
    private final String account;
    private final String facebookAccessToken;

    public LinkFacebookRequest(final String account, final String facebookAccessToken) {
        this.account = account;
        this.facebookAccessToken = facebookAccessToken;
    }

    public LinkFacebookRequest(final String account) {
        this.account = account;
        this.facebookAccessToken = null;
    }
}
