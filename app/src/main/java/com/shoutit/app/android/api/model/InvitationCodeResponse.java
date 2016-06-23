package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class InvitationCodeResponse {

    @Nonnull
    private final String id;
    @Nonnull
    private final String code;

    public InvitationCodeResponse(@Nonnull String id, @Nonnull String code) {
        this.id = id;
        this.code = code;
    }

    @Nonnull
    public String getCode() {
        return code;
    }
}
