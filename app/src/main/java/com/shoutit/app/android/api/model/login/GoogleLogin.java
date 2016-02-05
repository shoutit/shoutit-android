package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class GoogleLogin extends BaseLoginRequest {

    private static final String GOOGLE_LOGIN = "gplus_code";

    private final String gplusCode;

    public GoogleLogin(@NonNull String token, @Nullable LoginUser loginUser) {
        super(GOOGLE_LOGIN, loginUser);
        this.gplusCode = token;
    }
}