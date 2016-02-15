package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class BaseLoginRequest {

    private final String clientId = "shoutit-android";
    private final String clientSecret = "319d412a371643ccaa9166163c34387f";
    private final String mixpanelDistinctId = "67da5c7b-8312-4dc5-b7c2-f09b30aa7fa1";
    private final String grantType;
    private final LoginUser user;

    public BaseLoginRequest(@NonNull String grantType, @Nullable LoginUser user) {
        this.grantType = grantType;
        this.user = user;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getMixpanelDistinctId() {
        return mixpanelDistinctId;
    }

    public String getGrantType() {
        return grantType;
    }

    public LoginUser getUser() {
        return user;
    }
}
