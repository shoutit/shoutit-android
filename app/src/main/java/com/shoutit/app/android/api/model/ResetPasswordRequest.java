package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class ResetPasswordRequest {
    @Nonnull
    private final String email;

    public ResetPasswordRequest(@Nonnull String email) {
        this.email = email;
    }
}
