package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;

import com.shoutit.app.android.api.ApiConsts;

public class RefreshTokenRequest {
    @NonNull
    private final String clientId = ApiConsts.CLIENT_ID;
    @NonNull
    private final String clientSecret = ApiConsts.CLIENT_SECRET;
    @NonNull
    private final String grantType = ApiConsts.GRANT_TYPE_REFRESH_TOKEN;
    @NonNull
    private final String refreshToken;

    public RefreshTokenRequest(@NonNull String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
