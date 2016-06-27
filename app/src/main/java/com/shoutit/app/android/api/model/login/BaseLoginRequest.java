package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.annotation.Nonnull;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class BaseLoginRequest {

    private final String clientId = "shoutit-android";
    private final String clientSecret = "319d412a371643ccaa9166163c34387f";
    private final String mixpanelDistinctId;
    private final String grantType;
    @Nullable
    private final String invitationCode;
    private final LoginProfile profile;

    public BaseLoginRequest(@Nonnull String mixpanelDistinctId,
                            @NonNull String grantType,
                            @Nullable LoginProfile user,
                            @Nullable String invitationCode) {
        this.mixpanelDistinctId = mixpanelDistinctId;
        this.grantType = grantType;
        this.profile = user;
        this.invitationCode = invitationCode;
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

    public LoginProfile getProfile() {
        return profile;
    }
}
