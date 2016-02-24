package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

public class UpdateUserRequest {

    @Nullable
    private final String email;

    public UpdateUserRequest(@Nullable String email) {
        this.email = email;
    }

}
