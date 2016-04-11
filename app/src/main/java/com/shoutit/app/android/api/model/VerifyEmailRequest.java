package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class VerifyEmailRequest {
    @Nonnull
    private final String email;

    public VerifyEmailRequest(@Nonnull String email) {
        this.email = email;
    }
}
