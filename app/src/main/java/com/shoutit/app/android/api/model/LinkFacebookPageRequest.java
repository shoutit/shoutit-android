package com.shoutit.app.android.api.model;

import javax.annotation.Nonnull;

public class LinkFacebookPageRequest {

    @Nonnull
    private final String facebookPageId;

    public LinkFacebookPageRequest(@Nonnull String facebookPageId) {
        this.facebookPageId = facebookPageId;
    }
}
