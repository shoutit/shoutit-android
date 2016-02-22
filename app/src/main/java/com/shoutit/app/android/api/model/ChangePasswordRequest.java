package com.shoutit.app.android.api.model;

import android.support.annotation.Nullable;

import javax.annotation.Nonnull;

public class ChangePasswordRequest {

    @Nullable
    private final String oldPassword;
    @Nonnull
    private final String newPassword;
    @Nonnull
    private final String newPassword2;

    public ChangePasswordRequest(@Nullable String oldPassword, @Nonnull String newPassword, @Nonnull String newPassword2) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.newPassword2 = newPassword2;
    }
}
