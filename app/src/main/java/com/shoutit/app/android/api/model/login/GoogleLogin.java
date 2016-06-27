package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class GoogleLogin extends BaseLoginRequest {

    private static final String GOOGLE_LOGIN = "gplus_code";

    private final String gplusCode;

    public GoogleLogin(@NonNull String token,
                       @Nullable LoginProfile loginUser,
                       @NonNull String mixpanelDistinctId,
                       @Nullable String invitationCode) {
        super(mixpanelDistinctId, GOOGLE_LOGIN, loginUser, invitationCode);
        this.gplusCode = token;
    }
}