package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class FacebookLogin extends BaseLoginRequest {

    private static final String FACEBOOK_SIGNIN = "facebook_access_token";

    @NonNull
    private final String facebookAccessToken;

    public FacebookLogin(@NonNull String token,
                         @Nullable LoginProfile loginProfile,
                         @NonNull String mixpanelDistinctId) {
        super(mixpanelDistinctId, FACEBOOK_SIGNIN, loginProfile);
        this.facebookAccessToken = token;
    }
}