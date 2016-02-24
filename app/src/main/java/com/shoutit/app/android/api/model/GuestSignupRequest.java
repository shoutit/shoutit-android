package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import com.shoutit.app.android.api.model.login.BaseLoginRequest;
import com.shoutit.app.android.api.model.login.LoginUser;

public class GuestSignupRequest extends BaseLoginRequest {

    private static final String SHOUTIT_SIGNUP = "shoutit_guest";

    public GuestSignupRequest(@Nullable LoginUser user) {
        super(SHOUTIT_SIGNUP, user);
    }
}
