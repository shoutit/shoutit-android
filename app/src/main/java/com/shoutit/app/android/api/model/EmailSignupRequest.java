package com.shoutit.app.android.api.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.shoutit.app.android.api.model.login.BaseLoginRequest;
import com.shoutit.app.android.api.model.login.LoginProfile;

public class EmailSignupRequest extends BaseLoginRequest {

    private static final String SHOUTIT_SIGNUP = "shoutit_signup";

    @NonNull
    private final String name;
    @NonNull
    private final String email;
    @NonNull
    private final String password;

    public EmailSignupRequest(@NonNull String name,
                              @NonNull String email,
                              @NonNull String password,
                              @Nullable LoginProfile user,
                              @NonNull String mixpanelDistinctId) {
        super(mixpanelDistinctId, SHOUTIT_SIGNUP, user);
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getName() {
        return name;
    }
}
