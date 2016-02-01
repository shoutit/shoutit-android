package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class EmailLoginRequest extends BaseLoginRequest {

    private static final String SHOUTIT_SIGNIN = "shoutit_signin";

    private final String email;
    private final String password;

    public EmailLoginRequest(@NonNull String email, @NonNull String password, @Nullable LoginUser loginUser) {
        super(SHOUTIT_SIGNIN, loginUser);
        this.email = email;
        this.password = password;
    }
}