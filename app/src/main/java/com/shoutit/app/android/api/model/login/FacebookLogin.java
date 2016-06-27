package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import retrofit2.http.HEAD;

@SuppressWarnings("unused")
public class FacebookLogin extends BaseLoginRequest {

    private static final String FACEBOOK_SIGNIN = "facebook_access_token";

    @NonNull
    private final String facebookAccessToken;

    public FacebookLogin(@NonNull String token,
                         @Nullable LoginProfile loginUser,
                         @NonNull String mixpanelDistinctId,
                         @Nullable String invitationCode) {
        super(mixpanelDistinctId, FACEBOOK_SIGNIN, loginUser, invitationCode);
        this.facebookAccessToken = token;
    }
}