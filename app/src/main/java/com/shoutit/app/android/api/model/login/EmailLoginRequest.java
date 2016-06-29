package com.shoutit.app.android.api.model.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class EmailLoginRequest extends BaseLoginRequest {

    private static final String SHOUTIT_LOGIN = "shoutit_login";

    private final String email;
    private final String password;

    public EmailLoginRequest(@NonNull String email,
                             @NonNull String password,
                             @Nullable LoginProfile loginUser,
                             @NonNull String mixpanelDistinctId) {
        super(mixpanelDistinctId, SHOUTIT_LOGIN, loginUser, null);
        this.email = email;
        this.password = password;
    }
}